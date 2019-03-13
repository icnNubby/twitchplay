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
import ru.nubby.playstream.di.ActivityScoped;
import ru.nubby.playstream.di.FragmentScoped;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.presentation.stream.chat.ChatContract;
import ru.nubby.playstream.presentation.stream.chat.ChatFragment;
import ru.nubby.playstream.presentation.stream.chat.ChatPresenter;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamContract;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamFragment;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamPresenter;

@Module
public abstract class StreamChatModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract ChatFragment chatFragment();

    @ActivityScoped
    @Binds
    abstract ChatContract.Presenter chatPresenter(ChatPresenter chatPresenter);

    @FragmentScoped
    @ContributesAndroidInjector
    abstract StreamFragment streamFragment();

    @ActivityScoped
    @Binds
    abstract StreamContract.Presenter streamPresenter(StreamPresenter streamPresenter);

    @Provides
    @ActivityScoped
    static Single<Stream> provideStreamUpdater(StreamChatActivity activity, Repository repository) {
        String jsonStream = null;
        Bundle extras = activity.getIntent().getExtras();
        if (extras != null) {
            jsonStream = extras.getString("stream_json");
        }
        if (jsonStream == null) {
            Toast.makeText(activity, activity.getText(R.string.error_no_stream_info_provided), Toast.LENGTH_SHORT).show();
            activity.finish(); //we cant start stream from nothing
        }
        Stream currentStream = new Gson().fromJson(jsonStream, Stream.class);
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