package ru.nubby.playstream.presentation.streamlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import androidx.appcompat.widget.Toolbar;
import dagger.android.support.DaggerAppCompatActivity;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.StreamListNavigationState;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.presentation.BaseActivity;
import ru.nubby.playstream.presentation.login.LoginActivity;
import ru.nubby.playstream.presentation.preferences.PreferencesActivity;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListFragment;

import static ru.nubby.playstream.model.StreamListNavigationState.MODE_FAVOURITES;
import static ru.nubby.playstream.model.StreamListNavigationState.MODE_TOP;


public class StreamListActivity extends BaseActivity
        implements StreamListActivityContract.View {


    @Inject
    StreamListActivityContract.Presenter mActivityPresenter;

    @Inject
    StreamListFragment mStreamListFragment;

    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;
    private StreamListNavigationState mStateNavbar;

    private boolean mIsNavigationReallyClicked = false;


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

        setSupportActionBar(findViewById(R.id.toolbar));
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.stream_list_navigation_favourites: {
                    mStateNavbar = MODE_FAVOURITES;
                    break;
                }
                case R.id.stream_list_navigation_top_streams: {
                    mStateNavbar = MODE_TOP;
                    break;
                }
            }
            mActivityPresenter.changedNavigationState(mStateNavbar, mIsNavigationReallyClicked);
            return true;
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityPresenter.subscribe(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityPresenter.unsubscribe();
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
    public void setNavBarState(StreamListNavigationState state) {
        mStateNavbar = state;
        setSelectedNavBarItem();
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
        return mActivityPresenter != null;
    }

    public StreamListNavigationState getNavigationState() {
        return mStateNavbar;
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
        mIsNavigationReallyClicked = true;
    }

    private void startLoggingActivity() {
        Intent startLogin = new Intent(this, LoginActivity.class);
        startActivity(startLogin);
    }

    private void startPreferencesActivity() {
        Intent startPrefs = new Intent(this, PreferencesActivity.class);
        startActivity(startPrefs);
    }

}
