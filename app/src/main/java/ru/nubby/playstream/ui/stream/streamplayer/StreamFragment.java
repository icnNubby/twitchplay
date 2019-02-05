package ru.nubby.playstream.ui.stream.streamplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ru.nubby.playstream.R;
import ru.nubby.playstream.utils.Quality;

public class StreamFragment extends Fragment implements StreamContract.View, PopupMenu.OnMenuItemClickListener {


    public interface StreamActivityCallbacks {
        void toggleFullscreen(boolean fullscreenOn);
        boolean getFullscreenState();
    }

    private StreamContract.Presenter mPresenter;
    private PlayerView mVideoView;
    private ExoPlayer mExoPlayer;
    private ImageButton mFullscreenToggle;
    private ImageButton mQualityMenuButton;
    private PopupMenu mResolutionsMenu;
    private ProgressBar mProgressBar;

    private StreamActivityCallbacks mActivityCallbacks;


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Quality quality = Quality.values()[item.getItemId()];
        mPresenter.playChosenQuality(quality);
        return true;
    }

    public static StreamFragment newInstance() {

        Bundle args = new Bundle();

        StreamFragment fragment = new StreamFragment();
        fragment.setArguments(args);
        return fragment;
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
    public void setQualitiesMenu(List<Quality> qualities) {
        mResolutionsMenu = new PopupMenu(getActivity(), mQualityMenuButton);
        for (Quality quality: qualities)
            mResolutionsMenu.getMenu().add(
                    1,
                    quality.ordinal(),
                    0,
                    quality.getQualityShortName(getActivity()));
        mResolutionsMenu.setOnMenuItemClickListener(this);
    }

    @Override
    public void displayLoading(boolean loadingState) {
        mProgressBar.setIndeterminate(loadingState);
        if (loadingState) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stream, container, false);
        mVideoView = fragmentView.findViewById(R.id.stream_player);
        if (mExoPlayer == null)
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity());
        mVideoView.setPlayer(mExoPlayer);
        mVideoView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

        mFullscreenToggle = fragmentView.findViewById(R.id.fullscreen_toggle);
        mFullscreenToggle.setOnClickListener(v -> mActivityCallbacks.toggleFullscreen(!mActivityCallbacks.getFullscreenState()));

        mQualityMenuButton = fragmentView.findViewById(R.id.qualities_menu);
        mQualityMenuButton.setOnClickListener(v -> {
            if (mResolutionsMenu != null)
                mResolutionsMenu.show();
        });

        mProgressBar = fragmentView.findViewById(R.id.stream_buffer_playerview_progressbar);

        setRetainInstance(true);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivityCallbacks = (StreamActivityCallbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityCallbacks = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
        mVideoView.onPause();
        mExoPlayer.setPlayWhenReady(false);
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

    public void toggleFullscreen(boolean currentModeFullscreenOn) {
        if (currentModeFullscreenOn) {
            //turn on fullscreen, rotate to landscape, hide chat
            mVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        } else {
            //turn off fullscreen
            mVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
        redrawFullscreenButton(currentModeFullscreenOn);
    }

    private void redrawFullscreenButton(boolean currentModeFullscreenOn) {
        if (currentModeFullscreenOn) {
            mFullscreenToggle.setImageDrawable(
                    getResources().getDrawable(R.drawable.exo_controls_fullscreen_exit));
        } else {
            mFullscreenToggle.setImageDrawable(
                    getResources().getDrawable(R.drawable.exo_controls_fullscreen_enter));
        }
    }
}