package ru.nubby.playstream.data.twitchapi.oauth;

import java.net.HttpURLConnection;

import retrofit2.HttpException;

/**
 * Default error checker that checks for 401 for expired access token and 401 + 400 for bad refresh of token
 */
public class ErrorChecker {
    public boolean invalidAccessToken(Throwable throwable) {
        if (throwable instanceof HttpException) {
            return ((HttpException) throwable).code() == HttpURLConnection.HTTP_UNAUTHORIZED;
        }
        return false;
    }

    public boolean invalidRefreshToken(Throwable throwable) {
        if (throwable instanceof HttpException) {
            return (((HttpException) throwable).code() == HttpURLConnection.HTTP_UNAUTHORIZED) ||
                    (((HttpException) throwable).code() == HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return false;
    }
}
