package ru.nubby.playstream.domain.twitchapi.interceptors;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.nubby.playstream.SensitiveStorage;

public final class RequestTokenInterceptor implements Interceptor {
    @Override
    @NonNull
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Request newRequest;

        newRequest = request.newBuilder()
                .addHeader(SensitiveStorage.getHeaderClientId(),
                        SensitiveStorage.getClientApiKey())
                .build();
        return chain.proceed(newRequest);
    }
}