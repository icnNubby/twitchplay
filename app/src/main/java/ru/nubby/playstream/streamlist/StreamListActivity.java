package ru.nubby.playstream.streamlist;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import ru.nubby.playstream.R; //TODO FIX


public class StreamListActivity extends AppCompatActivity {

    StreamListContract.Presenter mPresenter;

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

        mPresenter = new StreamListPresenter(fragmentStreamList);

    }

}
