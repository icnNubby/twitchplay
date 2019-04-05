package ru.nubby.playstream.domain.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * DTO to store chat messages.
 */
public final class ChatMessage {
    @NonNull
    private String message;
    @NonNull
    private String user;
    @NonNull
    private String color;

    public ChatMessage(@NonNull String user, @NonNull String message, @NonNull String color) {
        this.message = message;
        this.user = user;
        this.color = color;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NonNull String message) {
        this.message = message;
    }

    @NonNull
    public String getUser() {
        return user;
    }

    public void setUser(@NonNull String user) {
        this.user = user;
    }

    @NonNull
    public String getColor() {
        return color;
    }

    public void setColor(@NonNull String color) {
        this.color = color;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof ChatMessage)) return false;
        return user.equals(((ChatMessage) obj).user) &&
                message.equals(((ChatMessage) obj).message) &&
                color.equals(((ChatMessage) obj).color);
    }

    @Override
    public int hashCode() {
        int hashCode = this.user.hashCode();
        hashCode = hashCode * 31 + this.message.hashCode();
        hashCode = hashCode * 31 + this.color.hashCode();
        return hashCode;
    }

    public boolean isEmpty(){
        return user.isEmpty() && color.isEmpty() && message.isEmpty();
    }
}
