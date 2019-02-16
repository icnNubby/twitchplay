package ru.nubby.playstream.ircapi;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import ru.nubby.playstream.model.ChatMessage;

public class ChatChannelApi {
    private final String TAG = getClass().getSimpleName();

    // Default Twitch Chat connect IP/domain and port
    private final String TWITCH_CHAT_SERVER = "irc.twitch.tv";
    private final int TWITCH_CHAT_PORT = 6667;


    private static final Pattern stdVarPattern =
            Pattern.compile("color=(#?\\w*);display-name=(\\w+).*;mod=(0|1);room-id=\\d+;.*subscriber=(0|1);.*turbo=(0|1);.* PRIVMSG #\\S* :(.*)");

    private String user;
    private String oauthKey;
    private String channelName;
    private Socket socket;

    private boolean connected;

    private BufferedWriter writer;
    private BufferedReader reader;

    private Observable<String> readerObservable;


    public ChatChannelApi(String user, String oauthKey, String channelName) {
        this.user = user;
        this.oauthKey = oauthKey;
        this.channelName = channelName.toLowerCase();
    }

    public Maybe<Boolean> init() {
        return Maybe.create(maybeEmitter -> {
            try {
                socket = new Socket(TWITCH_CHAT_SERVER, TWITCH_CHAT_PORT);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String loginRequest =
                        "PASS " + oauthKey + "\r\n" +
                                "NICK " + user + "\r\n" +
                                "USER " + user + " \r\n";
                sendMessage(loginRequest);
                readerObservable = Observable
                        .create(emitter -> {
                            String line;
                            try {
                                while (!emitter.isDisposed() && (line = reader.readLine()) != null)
                                    emitter.onNext(line);
                            } catch (IOException e) {
                                emitter.onError(e);
                            }
                        });
                maybeEmitter.onSuccess(true);
                Log.d(TAG, "Connected: ");
            } catch (UnknownHostException e) {
                maybeEmitter.onError(e);
                e.printStackTrace();
                Log.e(TAG, "Error: " + e, e);
            }
        });
    }

    public Observable<ChatMessage> listenToChat() {
        return readerObservable
                .map(line -> {
                    if (line.contains("376") && !connected) {
                        connected = true;
                        sendMessage("CAP REQ :twitch.tv/tags twitch.tv/commands" + "\r\n");
                        sendMessage("JOIN #" + channelName + "\r\n");
                    } else if (line.startsWith("PING")) {
                        sendMessage("PONG " + line.substring(5) + "\r\n");
                    } else if (line.contains("PRIVMSG")) {
                        Matcher matcher = stdVarPattern.matcher(line);
                        if (matcher.find()) {
                            String color = matcher.group(1);
                            String author = matcher.group(2);
                            //TODO include other stuff
                            String message = matcher.group(6);
                            return new ChatMessage(author, message, color);
                        } else {
                            Log.d(TAG, "No pattern found in message: " + line);
                        }
                    } else if (line.toLowerCase().contains("disconnected")) {
                        //TODO think about reconnect
                    }
                    return new ChatMessage("", "", "");
                })
                .filter(message -> !message.isEmpty())
                .doFinally(this::closeConnection);

    }

    public void closeConnection() throws IOException {
        if (connected) {
            reader.close();
            writer.close();
            socket.close();
            connected = false;
        }
    }

    private void sendMessage(String msg) throws IOException {
        writer.write(msg);
        writer.flush();
    }

}
