package ru.nubby.playstream.ircapi;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import ru.nubby.playstream.model.ChatMessage;

public class ChatChannelApi {
    private final String TAG = getClass().getSimpleName();

    // Default Twitch Chat connect IP/domain and port
    private final String twitchChatServer = "irc.twitch.tv";
    private final int twitchChatPort = 6667;

    private static final Pattern stdVarPattern =
            Pattern.compile("color=(#?\\w*);display-name=(\\w+).*;mod=(0|1);room-id=\\d+;.*subscriber=(0|1);.*turbo=(0|1);.* PRIVMSG #\\S* :(.*)");

    private String user;
    private String oauthKey;
    private String channelName;
    private Socket socket;

    private BufferedWriter writer;
    private BufferedReader reader;

    public ChatChannelApi(String user, String oauth_key, String channelName) {
        this.user = user;
        this.oauthKey = oauth_key;
        this.channelName = channelName.toLowerCase();
    }

    public Observable<ChatMessage> connect() {
        //TODO make it better, please
        return Observable.create(emitter -> {
            try {
                socket = new Socket(twitchChatServer, twitchChatPort);
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    writer.write("PASS " + oauthKey + "\r\n");
                    writer.write("NICK " + user + "\r\n");
                    writer.write("USER " + user + " \r\n");
                    writer.flush();
                    String line;
                    boolean connected = false;
                    while (!emitter.isDisposed() && (line = reader.readLine()) != null) {
                        if (line.contains("376") && !connected) {
                            writer.write("CAP REQ :twitch.tv/tags twitch.tv/commands" + "\r\n");
                            writer.flush();
                            writer.write("JOIN #" + channelName + "\r\n");
                            writer.flush();
                            connected = true;
                        } else if (line.startsWith("PING")) {
                            writer.write("PONG " + line.substring(5));
                            writer.flush();
                        } else if (line.contains("PRIVMSG")) {
                            Matcher matcher = stdVarPattern.matcher(line);
                            if (matcher.find()) {
                                String color = matcher.group(1);
                                String author = matcher.group(2);
                                //TODO include other stuff
                                String message = matcher.group(6);
                                emitter.onNext(new ChatMessage(author, message, color));
                            } else {
                                Log.d(TAG, "No pattern found in message: " + line);
                            }
                        }
                    }
                    emitter.onComplete();
                } catch (Throwable e) {
                    emitter.onError(e);
                } finally {
                    reader.close();
                    writer.close();
                }
            } catch (Throwable e) {
                emitter.onError(e);
            } finally {
                socket.close();
            }
        });
    }

    public void sendMessage(String message) {

    }

}
