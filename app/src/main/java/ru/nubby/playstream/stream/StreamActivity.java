package ru.nubby.playstream.stream;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

    private FrameLayout mChatContainer;
    private FrameLayout mPlayerContainer;
    private LinearLayout mStreamLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        mChatContainer = findViewById(R.id.fragment_chat_container);
        mPlayerContainer = findViewById(R.id.fragment_player_container);
        mStreamLinearLayout = findViewById(R.id.fragment_stream_linear_layout);


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
        setWindowMode(getResources().getConfiguration().orientation);
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
        setWindowMode(newConfig.orientation);
    }



    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void setWindowMode(int screenOrientation) {
        switch (screenOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:{
                mStreamLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                mPlayerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        5));
                mChatContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        2));
                mChatContainer.setVisibility(View.GONE);
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT: {
                mStreamLinearLayout.setOrientation(LinearLayout.VERTICAL);
                mPlayerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
                mChatContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0));
                mChatContainer.setVisibility(View.VISIBLE);
            }
        }
    }

}
