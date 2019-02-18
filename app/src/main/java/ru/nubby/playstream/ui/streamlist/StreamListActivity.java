package ru.nubby.playstream.ui.streamlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.nubby.playstream.R;
import ru.nubby.playstream.data.GlobalRepository;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.data.database.AppDatabase;
import ru.nubby.playstream.data.database.LocalDataSourceImpl;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.ui.login.LoginActivity;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListPresenter;

import static ru.nubby.playstream.ui.streamlist.StreamListNavigationState.MODE_FAVOURITES;


public class StreamListActivity extends AppCompatActivity implements StreamListActivityContract.View {

    private final String BUNDLE_NAVBAR_STATE = "navbar_state";
    private final String BUNDLE_TIME_STATE = "paused_at";
    private final StreamListNavigationState DEFAULT_NAVIGATION_STATE = MODE_FAVOURITES; //TODO PREFS

    private StreamListContract.Presenter mFragmentPresenter;
    private StreamListActivityContract.Presenter mActivityPresenter;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;
    private StreamListNavigationState stateNavbar = DEFAULT_NAVIGATION_STATE;

    private long pausedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_list);
        mToolbar = findViewById(R.id.toolbar);

        StreamListFragment fragmentStreamList = (StreamListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragmentStreamList == null) {
            fragmentStreamList = StreamListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentStreamList)
                    .commit();
        }

        Repository globalRepo =  GlobalRepository.getInstance(); //TODO INJECT
        boolean forceReload = false;
        if (savedInstanceState != null) {
            stateNavbar = (StreamListNavigationState) savedInstanceState.get(BUNDLE_NAVBAR_STATE);
        }
        if (!fragmentStreamList.hasPresenterAttached() || forceReload) {
            mFragmentPresenter = new StreamListPresenter(fragmentStreamList,
                    stateNavbar,
                    true,
                    globalRepo); //TODO inject
        } else {
            mFragmentPresenter = fragmentStreamList.returnAttachedPresenter();
        }

        new StreamListActivityPresenter(this, globalRepo); //TODO inject

        setSupportActionBar(findViewById(R.id.toolbar));
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        setSelectedNavBarItem();
        mBottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.stream_list_navigation_favourites: {
                    mFragmentPresenter.getFollowedStreams();
                    stateNavbar = MODE_FAVOURITES;
                    break;
                }
                case R.id.stream_list_navigation_top_streams: {
                    mFragmentPresenter.getTopStreams();
                    stateNavbar = StreamListNavigationState.MODE_TOP;
                    break;
                }
            }
            return true;
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        decideToUpdate(savedInstanceState.getLong(BUNDLE_TIME_STATE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityPresenter.subscribe();
        if (pausedAt > 0) decideToUpdate(pausedAt);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityPresenter.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_NAVBAR_STATE, stateNavbar);
        outState.putLong(BUNDLE_TIME_STATE, SystemClock.elapsedRealtime());
        pausedAt = SystemClock.elapsedRealtime();
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
        }
        return true;
    }

    @Override
    public void displayLoggedStatus(UserData user) {
        if (!user.isEmpty()) {
            mToolbar.setTitle(getString(R.string.logged_in) + user.getLogin());
            mBottomNavigationView.getMenu().findItem(R.id.stream_list_navigation_favourites).setEnabled(true);
        } else {
            mToolbar.setTitle(getString(R.string.not_logged_in));
            mBottomNavigationView.getMenu().findItem(R.id.stream_list_navigation_favourites).setEnabled(false);
            mBottomNavigationView.setSelectedItemId(R.id.stream_list_navigation_top_streams);
        }
    }

    @Override
    public void setPresenter(StreamListActivityContract.Presenter presenter) {
        mActivityPresenter = presenter;
    }

    @Override
    public boolean hasPresenterAttached() {
        return mActivityPresenter != null && mFragmentPresenter != null;
    }

    private void setSelectedNavBarItem() {
        int currentNavbarItemRId;
        switch (stateNavbar) {
            case MODE_FAVOURITES:
                currentNavbarItemRId = R.id.stream_list_navigation_favourites;
                break;
            case MODE_TOP:
                currentNavbarItemRId = R.id.stream_list_navigation_top_streams;
                break;
            default:
                currentNavbarItemRId = R.id.stream_list_navigation_favourites;
                stateNavbar = DEFAULT_NAVIGATION_STATE;
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

}
