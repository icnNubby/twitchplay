package ru.nubby.playstream.presentation.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PreferencesActivity extends AppCompatActivity implements PreferencesContract.View {
    PreferencesContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new PreferencesActivityPresenter(this);
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
