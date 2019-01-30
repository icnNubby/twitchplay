package ru.nubby.playstream.stream;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ru.nubby.playstream.R;

public class StreamFragment extends Fragment implements StreamContract.View {

    private final static String BUNDLE_FULLSCREEN_ON = "fullscreen_on";

    private StreamContract.Presenter mPresenter;
    private PlayerView mVideoView;
    private ExoPlayer mExoPlayer;
    private ImageButton mFullscreenToggle;
    private boolean fullscreenOn;


    public static StreamFragment newInstance() {

        Bundle args = new Bundle();

        StreamFragment fragment = new StreamFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stream, container, false);
        mVideoView = fragmentView.findViewById(R.id.stream_player);
        if (mExoPlayer == null)
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity());
        mVideoView.setPlayer(mExoPlayer);
        mFullscreenToggle = fragmentView.findViewById(R.id.fullscreen_toggle);
        mFullscreenToggle.setOnClickListener(v -> {
            toggleFullscreen(fullscreenOn);
        });
        if (savedInstanceState != null) {
            fullscreenOn = savedInstanceState.getBoolean(BUNDLE_FULLSCREEN_ON);
            redrawFullscreenButton();
        }
        setRetainInstance(true);
        return fragmentView;
    }

    @Override
    public void displayStream(String url) {
        if (url == null || "".equals(url)) {
            //TODO no stream error processing
            return;
        }
        getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);

        String userAgent = Util.getUserAgent(getActivity(),
                getActivity().getApplicationContext().getApplicationInfo().packageName);

        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent,
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true);

        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(getActivity(), null, httpDataSourceFactory);

        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url));
        mExoPlayer.prepare(videoSource);
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_FULLSCREEN_ON, fullscreenOn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExoPlayer.release();
    }

    @Override
    public void setPresenter(StreamContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void toggleFullscreen(boolean fullscreenIsOn) {
        if (!fullscreenIsOn) {
            //turn on fullscreen, rotate to landscape, hide chat
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            mVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        } else {
            //turn off fullscreen
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            mVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
        fullscreenOn = !fullscreenOn;
        redrawFullscreenButton();
    }

    private void redrawFullscreenButton() {
        if (fullscreenOn) {
            mFullscreenToggle.setImageDrawable(
                    getResources().getDrawable(R.drawable.exo_controls_fullscreen_exit));
        } else {
            mFullscreenToggle.setImageDrawable(
                    getResources().getDrawable(R.drawable.exo_controls_fullscreen_enter));
        }
    }
}
/*
* ТЗ
    при включенном фуллскрине
        Фрагмент чата справа(можно скрыть), ориентация залочена на лендскейп, включены повороты на 180 соответственно.
    при выключенном фуллскрине
        фрагмент чата снизу при портрете
        фрагмент чата справа при лендскейпе(можно скрыть)
        повороты по 90 градусов.
    реализовать переход от сплитскрина на 2 фрагмента до фуллскрина в лендскейпе по свайпу + по кнопке справа.
	*/
