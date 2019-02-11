package ru.nubby.playstream.ui.streamlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.nubby.playstream.R; //TODO FIX
import ru.nubby.playstream.ui.login.LoginActivity;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListPresenter;


public class StreamListActivity extends AppCompatActivity implements StreamListActivityContract.View {

    StreamListContract.Presenter mFragmentPresenter;
    StreamListActivityContract.Presenter mActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_list);

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
    public void displayLoggedStatus(String loggedName, boolean logged) {
        
    }

    private void startLoggingActivity() {
        Intent startLogin = new Intent(this, LoginActivity.class);
        //TODO fix to some constant
        startActivityForResult(startLogin, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            //mActivityPresenter.login(token);
        }
    }

    @Override
    public void setPresenter(StreamListActivityContract.Presenter presenter) {
        mActivityPresenter = presenter;
    }
}
