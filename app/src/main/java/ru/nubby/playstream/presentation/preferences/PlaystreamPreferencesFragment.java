package ru.nubby.playstream.presentation.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import javax.inject.Inject;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import ru.nubby.playstream.R;
import ru.nubby.playstream.presentation.preferences.utils.TimePreference;
import ru.nubby.playstream.presentation.preferences.utils.TimePreferenceDialogFragmentCompat;
import ru.nubby.playstream.services.NotificationService;
import ru.nubby.playstream.services.ServicesScheduler;

public class PlaystreamPreferencesFragment extends PreferenceFragmentCompat
        implements PreferencesContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    PreferencesContract.Presenter mPresenter;

    @Inject
    Context mContext;

    @Inject
    ServicesScheduler mServicesScheduler;

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

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            dialogFragment = new TimePreferenceDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "androidx.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //todo ?
    }
}
