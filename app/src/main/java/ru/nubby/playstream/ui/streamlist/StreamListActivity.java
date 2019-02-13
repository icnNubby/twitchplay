package ru.nubby.playstream.ui.streamlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.nubby.playstream.R; //TODO FIX
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.ui.login.LoginActivity;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListPresenter;


public class StreamListActivity extends AppCompatActivity implements StreamListActivityContract.View {

    private static final int LOGIN_REQUEST_CODE = 101;

    private StreamListContract.Presenter mFragmentPresenter;
    private StreamListActivityContract.Presenter mActivityPresenter;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;

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

        mFragmentPresenter = new StreamListPresenter(fragmentStreamList); //TODO inject
        new StreamListActivityPresenter(this); //TODO inject
        setSupportActionBar(findViewById(R.id.toolbar));
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.stream_list_navigation_favourites: {
                    mFragmentPresenter.getFollowedStreams();
                    break;
                }
                case R.id.stream_list_navigation_top_streams: {
                    mFragmentPresenter.getTopStreams();
                    break;
                }
            }
            return true;
        });

        //TODO bundle
        mBottomNavigationView.setSelectedItemId(R.id.stream_list_navigation_top_streams);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityPresenter.subscribe();
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
        }
        return true;
    }

    @Override
    public void displayLoggedStatus(UserData user, boolean logged) {
        if (logged) {
            mToolbar.setTitle(getString(R.string.logged_in) + user.getLogin());
            mBottomNavigationView.getMenu().findItem(R.id.stream_list_navigation_favourites).setEnabled(true);
        } else {
            mToolbar.setTitle(getString(R.string.not_logged_in));
            mBottomNavigationView.getMenu().findItem(R.id.stream_list_navigation_favourites).setEnabled(false);
        }
    }

    private void startLoggingActivity() {
        Intent startLogin = new Intent(this, LoginActivity.class);
        startActivity(startLogin);
    }

    @Override
    public void setPresenter(StreamListActivityContract.Presenter presenter) {
        mActivityPresenter = presenter;
    }
}
