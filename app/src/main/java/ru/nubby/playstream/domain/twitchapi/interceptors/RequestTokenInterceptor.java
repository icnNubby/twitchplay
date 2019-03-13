package ru.nubby.playstream.domain.twitchapi.interceptors;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.nubby.playstream.SensitiveStorage;

/**
 * An interceptor that adds custom header to all request
 */
public final class RequestTokenInterceptor implements Interceptor {

    private final String mHeaderName;
    private final String mHeaderValue;

    public RequestTokenInterceptor(String headerName, String headerValue) {
        mHeaderName = headerName;
        mHeaderValue = headerValue;
    }

    @Override
    @NonNull
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Request newRequest;

        newRequest = request.newBuilder()
                .addHeader(mHeaderName, mHeaderValue)
                .build();
        return chain.proceed(newRequest);
    }
}