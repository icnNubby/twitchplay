package ru.nubby.playstream.ui.stream;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Single;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.ui.stream.chat.ChatFragment;
import ru.nubby.playstream.ui.stream.chat.ChatPresenter;
import ru.nubby.playstream.ui.stream.streamplayer.StreamFragment;
import ru.nubby.playstream.ui.stream.streamplayer.StreamPresenter;
import ru.nubby.playstream.ui.uihelpers.OnSwipeTouchListener;

/**
 * Should be called with extra JSON : gsonned model.Stream object
 */
public class StreamChatActivity extends AppCompatActivity implements StreamFragment.StreamActivityCallbacks {

    private final static String BUNDLE_FULLSCREEN_ON = "fullscreen_on";

    private FrameLayout mChatContainer;
    private FrameLayout mPlayerContainer;
    private LinearLayout mStreamLinearLayout;
    private OnSwipeTouchListener mOnSwipeTouchListener;

    private StreamFragment mStreamFragment;

    private boolean fullscreenOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String jsonStream = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonStream = extras.getString("stream_json");
        }
        if (jsonStream == null) {
            Toast.makeText(this, getText(R.string.error_no_stream_info_provided), Toast.LENGTH_SHORT).show();
            finish(); //we cant start stream from nothing
        }
        Stream currentStream = new Gson().fromJson(jsonStream, Stream.class); //TODO inject?


        setContentView(R.layout.activity_stream);
        mChatContainer = findViewById(R.id.fragment_chat_container);
        mPlayerContainer = findViewById(R.id.fragment_player_container);
        mStreamLinearLayout = findViewById(R.id.fragment_stream_linear_layout);
        mOnSwipeTouchListener = new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                showChat();
            }

            @Override
            public void onSwipeRight() {
                hideChat();
            }

            @Override
            public void onDoubleTapListener() {
                toggleFullscreen(!fullscreenOn);
            }
        };
        mStreamLinearLayout.setOnTouchListener(mOnSwipeTouchListener);


        StreamFragment streamFragment = (StreamFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player_container);

        if (streamFragment == null) {
            streamFragment = StreamFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_player_container, streamFragment)
                    .commit();
        }
        mStreamFragment = streamFragment;

        if (savedInstanceState != null) {
            fullscreenOn = savedInstanceState.getBoolean(BUNDLE_FULLSCREEN_ON);
            toggleFullscreen(fullscreenOn);
        }

        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_chat_container);

        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_chat_container, chatFragment)
                    .commit();
        }

        //TODO !THINK HOW TO DECOUPLE THAT
        Single<Stream> currentStreamUpdate = new RemoteStreamFullInfo()
                .getStreamerInfo(currentStream)
                .map(updatedLogin -> {
                    currentStream.setStreamerLogin(updatedLogin.getLogin());
                    return currentStream;
                });
        //TODO !THINK HOW TO DECOUPLE THAT

        if (!streamFragment.hasPresenterAttached()) {
            new StreamPresenter(streamFragment, currentStreamUpdate);
        }
        if (!chatFragment.hasPresenterAttached()) {
            new ChatPresenter(chatFragment, currentStreamUpdate);
        }

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        Handler handler = new Handler();
                        handler.postDelayed(this::setAndroidUiMode, 3000);
                    } else {
                        hideSystemUI();
                    }
                });
        setWindowMode(getResources().getConfiguration().orientation);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(BUNDLE_FULLSCREEN_ON, fullscreenOn);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void toggleFullscreen(boolean fullscreenOn) {
        if (fullscreenOn) {
            //turn on fullscreen, rotate to landscape, hide chat
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            hideChat();
        } else {
            //turn off fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            showChat();
        }
        this.fullscreenOn = fullscreenOn;
        mStreamFragment.toggleFullscreen(fullscreenOn);
    }

    @Override
    public boolean getFullscreenState() {
        return fullscreenOn;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mOnSwipeTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void setAndroidUiMode() {
        View decorView = getWindow().getDecorView();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // Hide Status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide navigation bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // Hide Status bar
                );
            }
        } else {
            decorView.setSystemUiVisibility(0); // Remove all flags.
        }
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

    private void hideChat() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE &&
                !fullscreenOn)
            mChatContainer.setVisibility(View.GONE); //TODO ANIM
    }

    private void showChat() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE &&
                !fullscreenOn)
            mChatContainer.setVisibility(View.VISIBLE); //TODO ANIM
    }

    private void setWindowMode(int screenOrientation) {
        switch (screenOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE: {
                mStreamLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                mPlayerContainer.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                5));
                mChatContainer.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                2));
                mChatContainer.setVisibility(View.GONE);
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT: {
                mStreamLinearLayout.setOrientation(LinearLayout.VERTICAL);
                mPlayerContainer.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                0));
                mChatContainer.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                0));
                mChatContainer.setVisibility(View.VISIBLE);
            }
        }
    }

}
