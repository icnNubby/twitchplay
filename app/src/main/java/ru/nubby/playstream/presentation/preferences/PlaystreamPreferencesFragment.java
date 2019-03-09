package ru.nubby.playstream.presentation.preferences;

import android.os.Bundle;

import javax.inject.Inject;

import androidx.preference.PreferenceFragmentCompat;
import ru.nubby.playstream.R;

public class PlaystreamPreferencesFragment extends PreferenceFragmentCompat
        implements PreferencesContract.View {

    @Inject
    PreferencesContract.Presenter mPresenter;

    @Inject
    public PlaystreamPreferencesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_display);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe(this);
    }

    @Override
    public void onPause() {
        mPresenter.unsubscribe();
        super.onPause();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

}
