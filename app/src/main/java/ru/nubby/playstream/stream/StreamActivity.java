package ru.nubby.playstream.stream;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import ru.nubby.playstream.R;
import ru.nubby.playstream.chat.ChatFragment;
import ru.nubby.playstream.chat.ChatPresenter;
import ru.nubby.playstream.model.Stream;

/**
 * Should be called with extra JSON : gsonned model.Stream object
 */
public class StreamActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);


        StreamFragment streamFragment = (StreamFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player_container);

        if (streamFragment == null) {
            streamFragment = StreamFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_player_container, streamFragment)
                    .commit();
        }

        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_chat_container);

        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_chat_container, chatFragment)
                    .commit();
        }

        String jsonStream = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonStream = extras.getString("stream_json");
        }
        Stream currentStream = new Gson().fromJson(jsonStream, Stream.class);

        if (!streamFragment.hasPresenterAttached()) {
            new StreamPresenter(streamFragment, currentStream);
        }
        if (!chatFragment.hasPresenterAttached()) {
            new ChatPresenter(chatFragment);
        }

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        hideSystemUI();
                        // TODO: The system bars are visible. Make any desired
                        // TODO: implement delay
                    } else {
                        hideSystemUI();
                    }
                });
    }


    private void hideSystemUI() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //TODO think how to implement rotate without getting fucked by reattaching player to new surface
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

}
