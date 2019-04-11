package ru.nubby.playstream.presentation.stream;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import ru.nubby.playstream.R;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.presentation.base.BaseActivity;
import ru.nubby.playstream.presentation.stream.chat.ChatFragment;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamFragment;
import ru.nubby.playstream.presentation.base.custom.OnSwipeTouchListener;
import ru.nubby.playstream.utils.Constants;

/**
 * Should be called with extra JSON : gsonned model.Stream object
 */
public class StreamChatActivity extends BaseActivity
        implements StreamFragment.StreamActivityCallbacks {

    private final static String BUNDLE_FULLSCREEN_ON = "fullscreen_on";

    private FrameLayout mChatContainer;
    private FrameLayout mPlayerContainer;
    private LinearLayout mStreamLinearLayout;
    private OnSwipeTouchListener mOnSwipeTouchListener;

    @Inject
    StreamFragment mStreamFragment;

    @Inject
    ChatFragment mChatFragment;

    @Inject
    Gson mGson;

    private boolean mFullscreenOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stream);
        mChatContainer = findViewById(R.id.fragment_chat_container);
        mPlayerContainer = findViewById(R.id.fragment_player_container);
        mStreamLinearLayout = findViewById(R.id.fragment_stream_linear_layout);
        mOnSwipeTouchListener = new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeBottom() {
                //todo if possible make small window like a twitch app
            }

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
                toggleFullscreen(!mFullscreenOn);
            }
        };
        mStreamLinearLayout.setOnTouchListener(mOnSwipeTouchListener);


        StreamFragment streamFragment = (StreamFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player_container);

        if (streamFragment == null) {
            streamFragment = mStreamFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_player_container, streamFragment)
                    .commit();
        }
        mStreamFragment = streamFragment;

        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_chat_container);

        if (chatFragment == null) {
            chatFragment = mChatFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_chat_container, chatFragment)
                    .commit();
        }
        mChatFragment = chatFragment;

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

        Stream stream = readStreamFromExtras();

        //TODO think more
        mStreamFragment.setCurrentStream(stream);
        mChatFragment.setCurrentStream(stream);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mFullscreenOn = savedInstanceState.getBoolean(BUNDLE_FULLSCREEN_ON);
            toggleFullscreen(mFullscreenOn);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(BUNDLE_FULLSCREEN_ON, mFullscreenOn);
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
        this.mFullscreenOn = fullscreenOn;
        mStreamFragment.toggleFullscreen(fullscreenOn);
    }

    @Override
    public boolean getFullscreenState() {
        return mFullscreenOn;
    }

    @Override
    public void onBackPressed() {
        if (mFullscreenOn) {
            toggleFullscreen(!mFullscreenOn);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mOnSwipeTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void setAndroidUiMode() {
        View decorView = getWindow().getDecorView();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide navigation bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // Hide Status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
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
                !mFullscreenOn)
            mChatContainer.setVisibility(View.GONE); //TODO ANIM
    }

    private void showChat() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE &&
                !mFullscreenOn)
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

    private Stream readStreamFromExtras() {
        String jsonStream = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonStream = extras.getString(Constants.sStreamIntentKey);
        }
        if (jsonStream == null) {
            Toast.makeText(this, getText(R.string.error_no_stream_info_provided), Toast.LENGTH_SHORT).show();
            finish(); //we cant start stream from nothing
        }
        Stream currentStream = mGson.fromJson(jsonStream, Stream.class);
        if (currentStream == null) {
            finish();
        }
        return currentStream;
    }
}
