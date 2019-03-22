package ru.nubby.playstream.data.twitchapi.interceptors;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;

/**
 * An interceptor that allows runtime changes to the URL hostname.
 */
public final class HostSelectionInterceptor implements Interceptor {
    private volatile String host;

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String host = this.host;
        if (host != null && !host.isEmpty()) {
            HttpUrl newUrl = HttpUrl.parse(host);
            if (newUrl != null) {
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
            }
        }
        return chain.proceed(request);
    }

}