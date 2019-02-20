package ru.nubby.playstream.data.twitchapi.oauth;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.nubby.playstream.utils.SharedPreferencesManager;

/**
 * Interceptor that adds Authorisation header with access token to Http requests.
 */
public class OAuthInterceptor implements Interceptor {
    @Override
    @NonNull
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();
        if (!SharedPreferencesManager.getUserAccessToken().isEmpty()) {
            builder.addHeader("Authorization", "Bearer " +
                    SharedPreferencesManager.getUserAccessToken());
        }
        return chain.proceed(builder.build());
    }
}
