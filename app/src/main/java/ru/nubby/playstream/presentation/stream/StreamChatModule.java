package ru.nubby.playstream.presentation.stream;

import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import io.reactivex.Single;
import ru.nubby.playstream.R;
import ru.nubby.playstream.di.scopes.ActivityScope;
import ru.nubby.playstream.di.scopes.FragmentScope;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.presentation.stream.chat.ChatContract;
import ru.nubby.playstream.presentation.stream.chat.ChatFragment;
import ru.nubby.playstream.presentation.stream.chat.ChatPresenter;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamContract;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamFragment;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamPresenter;

@Module
public abstract class StreamChatModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract ChatFragment chatFragment();

    @FragmentScope
    @ContributesAndroidInjector
    abstract StreamFragment streamFragment();

    @ActivityScope
    @Binds
    abstract StreamContract.Presenter streamPresenter(StreamPresenter presenter);

    @ActivityScope
    @Binds
    abstract ChatContract.Presenter chatPresenter(ChatPresenter presenter);

    @Provides
    @ActivityScope
    static Single<Stream> provideStreamUpdater(StreamChatActivity activity,
                                               Repository repository,
                                               Gson gson) {
        String jsonStream = null;
        Bundle extras = activity.getIntent().getExtras();
        if (extras != null) {
            jsonStream = extras.getString("stream_json");
        }
        if (jsonStream == null) {
            Toast.makeText(activity, activity.getText(R.string.error_no_stream_info_provided), Toast.LENGTH_SHORT).show();
            activity.finish(); //we cant start stream from nothing
        }
        Stream currentStream = gson.fromJson(jsonStream, Stream.class);
        if (currentStream == null) {
            activity.finish();
        }

        return repository
                .getUserFromStreamer(currentStream)
                .map(updatedLogin -> {
                    if (currentStream != null) {
                        currentStream.setStreamerLogin(updatedLogin.getLogin());
                    }
                    return currentStream;
                });
    }

}
