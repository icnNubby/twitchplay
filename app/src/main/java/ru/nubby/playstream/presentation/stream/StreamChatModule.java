package ru.nubby.playstream.presentation.stream;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.FragmentScope;
import ru.nubby.playstream.presentation.stream.chat.ChatFragment;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamFragment;

@Module
public abstract class StreamChatModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract ChatFragment chatFragment();

    @FragmentScope
    @ContributesAndroidInjector
    abstract StreamFragment streamFragment();

}
