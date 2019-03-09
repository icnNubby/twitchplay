package ru.nubby.playstream.presentation.streamlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import dagger.android.support.DaggerAppCompatActivity;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.presentation.login.LoginActivity;
import ru.nubby.playstream.presentation.preferences.PreferencesActivity;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;

import static ru.nubby.playstream.presentation.streamlist.StreamListNavigationState.MODE_FAVOURITES;
import static ru.nubby.playstream.presentation.streamlist.StreamListNavigationState.MODE_TOP;


public class StreamListActivity extends DaggerAppCompatActivity
        implements StreamListActivityContract.View {

    private final String BUNDLE_NAVBAR_STATE = "navbar_state";
    private final String BUNDLE_TIME_STATE = "paused_at";

    @Inject
    StreamListContract.Presenter mFragmentPresenter;

    @Inject
    StreamListActivityContract.Presenter mActivityPresenter;

    @Inject
    StreamListFragment mStreamListFragment;

    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;
    private StreamListNavigationState mStateNavbar;

    private long mPausedAt;
    private boolean mFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_list);
        mToolbar = findViewById(R.id.toolbar);

        StreamListFragment fragmentStreamList = (StreamListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragmentStreamList == null) {
            fragmentStreamList = mStreamListFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentStreamList)
                    .commit();
        }
        mStreamListFragment = fragmentStreamList;


        if (savedInstanceState != null) {
            mStateNavbar = (StreamListNavigationState) savedInstanceState.get(BUNDLE_NAVBAR_STATE);
        }

        mFirstLoad = (savedInstanceState == null);

        setSupportActionBar(findViewById(R.id.toolbar));
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.stream_list_navigation_favourites: {
                    mFragmentPresenter.getFollowedStreams();
                    mStateNavbar = MODE_FAVOURITES;
                    break;
                }
                case R.id.stream_list_navigation_top_streams: {
                    mFragmentPresenter.getTopStreams();
                    mStateNavbar = MODE_TOP;
                    break;
                }
            }
            return true;
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPausedAt = savedInstanceState.getLong(BUNDLE_TIME_STATE);
        mFirstLoad = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityPresenter.subscribe(this);
        if (mPausedAt > 0) decideToUpdate(mPausedAt);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityPresenter.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_NAVBAR_STATE, mStateNavbar);
        outState.putLong(BUNDLE_TIME_STATE, SystemClock.elapsedRealtime());
        mPausedAt = SystemClock.elapsedRealtime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stream_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login: {
                startLoggingActivity();
                break;
            }
            case R.id.menu_preferences: {
                startPreferencesActivity();
                break;
            }
        }
        return true;
    }

    @Override
    public void setDefaultNavBarState(StreamListNavigationState state) {
        mStateNavbar = state;
    }

    @Override
    public void displayLoggedStatus(UserData user) {
        if (!user.isEmpty()) {
            mToolbar.setTitle(getString(R.string.logged_in) + user.getLogin());
            mBottomNavigationView.getMenu()
                    .findItem(R.id.stream_list_navigation_favourites)
                    .setEnabled(true);
        } else {
            mToolbar.setTitle(getString(R.string.not_logged_in));
            mBottomNavigationView.getMenu()
                    .findItem(R.id.stream_list_navigation_favourites)
                    .setEnabled(false);
            mBottomNavigationView.setSelectedItemId(R.id.stream_list_navigation_top_streams);
        }
    }

    @Override
    public boolean hasPresenterAttached() {
        return mActivityPresenter != null && mFragmentPresenter != null;
    }

    public StreamListNavigationState getNavigationState() {
        return mStateNavbar;
    }

    public boolean isFirstLoad(){
        return mFirstLoad;
    }

    private void setSelectedNavBarItem() {
        int currentNavbarItemRId;
        switch (mStateNavbar) {
            case MODE_FAVOURITES:
                currentNavbarItemRId = R.id.stream_list_navigation_favourites;
                break;
            case MODE_TOP:
                currentNavbarItemRId = R.id.stream_list_navigation_top_streams;
                break;
            default:
                currentNavbarItemRId = R.id.stream_list_navigation_favourites;
        }
        mBottomNavigationView.setSelectedItemId(currentNavbarItemRId);
    }

    private void startLoggingActivity() {
        Intent startLogin = new Intent(this, LoginActivity.class);
        startActivity(startLogin);
    }

    private void decideToUpdate(long savedTime) {
        if (mFragmentPresenter != null) {
            mFragmentPresenter.decideToReload(SystemClock.elapsedRealtime() - savedTime);
        }
    }

    private void startPreferencesActivity() {
        Intent startPrefs = new Intent(this, PreferencesActivity.class);
        startActivity(startPrefs);
    }

}
