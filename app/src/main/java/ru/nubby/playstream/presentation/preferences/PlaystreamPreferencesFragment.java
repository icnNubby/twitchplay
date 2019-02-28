package ru.nubby.playstream.presentation.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import ru.nubby.playstream.R;

public class PlaystreamPreferencesFragment extends PreferenceFragmentCompat
        implements PreferencesContract.View {

    private PreferencesContract.Presenter mPresenter;

    public static PlaystreamPreferencesFragment newInstance() {

        Bundle args = new Bundle();

        PlaystreamPreferencesFragment fragment = new PlaystreamPreferencesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_display);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void setPresenter(PreferencesContract.Presenter fragmentPresenter) {
        mPresenter = fragmentPresenter;
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

}
