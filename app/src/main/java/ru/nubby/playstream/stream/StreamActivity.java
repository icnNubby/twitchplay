package ru.nubby.playstream.stream;

import android.os.Bundle;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Stream;

/**
 * Should be called with extra JSON : gsonned model.Stream object
 */
public class StreamActivity extends AppCompatActivity {

    StreamContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        StreamFragment fragmentStreamList = (StreamFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (fragmentStreamList == null) {
            fragmentStreamList = StreamFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentStreamList)
                    .commit();
        }

        String jsonStream = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonStream = extras.getString("stream_json");
        }
        Stream currentStream = new Gson().fromJson(jsonStream, Stream.class);

        mPresenter = new StreamPresenter(fragmentStreamList, currentStream);

    }
}
