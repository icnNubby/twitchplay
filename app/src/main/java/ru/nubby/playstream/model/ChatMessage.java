package ru.nubby.playstream.model;

/**
 * POJO to store chat messages.
 */
public class ChatMessage {
    private String message;
    private String user;
    private String color;

    public ChatMessage(String user, String message, String color) {
        this.message = message;
        this.user = user;
        this.color = color;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
