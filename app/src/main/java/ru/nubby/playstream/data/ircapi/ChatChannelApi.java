package ru.nubby.playstream.data.ircapi;

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

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.model.ChatMessage;

public class ChatChannelApi {
    private final String TAG = getClass().getSimpleName();

    // Default Twitch Chat connect IP/domain and port
    private final String TWITCH_CHAT_SERVER = "irc.twitch.tv";
    private final int TWITCH_CHAT_PORT = 6667;

    private static final Pattern STD_VAR_PATTERN =
            Pattern.compile("color=(#?\\w*);display-name=(\\w+).*;mod=(0|1);room-id=\\d+;.*subscriber=(0|1);.*turbo=(0|1);.* PRIVMSG #\\S* :(.*)");

    private final String user;
    private final String oauthKey;
    private final String channelName;
    private Socket socket;

    private boolean connected;

    private BufferedWriter writer;
    private BufferedReader reader;

    private Observable<String> readerObservable;


    public ChatChannelApi(@NonNull String user,
                          @NonNull String oauthKey,
                          @NonNull String channelName) {
        this.user = user;
        this.oauthKey = oauthKey;
        this.channelName = channelName.toLowerCase();
    }

    public Single<Boolean> init() {

        //Single initialization emitter would emit true (success) if we instantiated
        //socket, reader and writer
        //if anything went wrong (socket connection, streams instantiation) it would emit error.

        return Single.create(singleEmitter -> {
            try {
                socket = new Socket(TWITCH_CHAT_SERVER, TWITCH_CHAT_PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String loginRequest = "PASS " + oauthKey + "\r\n" +
                        "NICK " + user + "\r\n" +
                        "USER " + user + " \r\n";
                sendMessage(loginRequest);

                //readerObservable will emit new line each time BufferedReader will provide any new
                //in case of IOException (probably because of net loss) we emit errors
                //in case of IOException + Disposal we suppose that it is normal closing state

                readerObservable = Observable
                        .create(emitter -> {
                            String line;
                            try {
                                while (!socket.isClosed() &&
                                        !emitter.isDisposed() &&
                                        (line = reader.readLine()) != null) {
                                    emitter.onNext(line);
                                }
                            } catch (IOException exception) {
                                if (!emitter.isDisposed()) {
                                    connected = false;
                                    emitter.onError(exception);
                                } else {
                                    Log.d(TAG, "Closing connections.");
                                }
                            }
                        });
                singleEmitter.onSuccess(true);
                Log.d(TAG, "Connected: ");
            } catch (UnknownHostException exception) {
                singleEmitter.onError(exception);
                Log.e(TAG, "UnknownHost error while connecting to chat: " + exception, exception);
            } catch (IOException exception) {
                singleEmitter.onError(exception);
                Log.e(TAG, "IOException error while connecting to chat: " + exception, exception);
            }
        });
    }

    public Observable<ChatMessage> listenToChat() {
        return readerObservable
                .map(line -> {
                    if (line.contains("376") && !connected) {
                        connected = true;
                        sendMessage("CAP REQ :twitch.tv/tags twitch.tv/commands");
                        sendMessage("JOIN #" + channelName);
                    } else if (line.startsWith("PING")) {
                        sendMessage("PONG " + line.substring(5));
                    } else if (line.contains("PRIVMSG")) {
                        Matcher matcher = STD_VAR_PATTERN.matcher(line);
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
                        connected = false;
                        //TODO think about reconnect
                    }
                    return new ChatMessage("", "", "");
                })
                .filter(message -> !message.isEmpty())
                .doOnDispose(this::closeConnection);

    }

    private synchronized void closeConnection() throws IOException {
        if (connected) {
            if (!socket.isClosed()) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
            //It WILL throw IOException, which will be catched while reading next line
            //https://stackoverflow.com/questions/3595926/how-to-interrupt-bufferedreaders-readline/8358072
            //Not the best way to handle it, yes.
            connected = false;
        }
    }

    private void sendMessage(String msg) throws IOException {
        writer.write(msg + "\r\n");
        writer.flush();
    }

}
