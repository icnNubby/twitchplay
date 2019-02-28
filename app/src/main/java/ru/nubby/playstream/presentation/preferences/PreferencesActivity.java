package ru.nubby.playstream.presentation.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.nubby.playstream.R;

public class PreferencesActivity extends AppCompatActivity{
    PreferencesContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        PlaystreamPreferencesFragment fragmentPreferences = (PlaystreamPreferencesFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentPreferences == null) {
            fragmentPreferences = PlaystreamPreferencesFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentPreferences)
                    .commit();
        }
        mPresenter = new PreferencesPresenter(fragmentPreferences);//todo inject
        Toolbar appBar = findViewById(R.id.toolbar);
        setSupportActionBar(appBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
