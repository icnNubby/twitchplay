package ru.nubby.playstream.presentation.preferences;

import android.os.Bundle;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import dagger.android.support.DaggerAppCompatActivity;
import ru.nubby.playstream.R;

public class PreferencesActivity extends DaggerAppCompatActivity {

    @Inject
    PreferencesContract.Presenter mPresenter;

    @Inject
    PlaystreamPreferencesFragment mPreferencesFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        PlaystreamPreferencesFragment fragmentPreferences = (PlaystreamPreferencesFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentPreferences == null) {
            fragmentPreferences = mPreferencesFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentPreferences)
                    .commit();
        }
        Toolbar appBar = findViewById(R.id.toolbar);
        setSupportActionBar(appBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
