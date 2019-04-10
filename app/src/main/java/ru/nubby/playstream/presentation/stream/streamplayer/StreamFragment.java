package ru.nubby.playstream.presentation.stream.streamplayer;

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
import android.widget.TextView;
import android.widget.Toast;

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

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import ru.nubby.playstream.R;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.presentation.base.BaseFragment;
import ru.nubby.playstream.presentation.base.PresenterFactory;

public class StreamFragment extends BaseFragment
        implements StreamContract.View, PopupMenu.OnMenuItemClickListener {


    public interface StreamActivityCallbacks {
        void toggleFullscreen(boolean fullscreenOn);

        boolean getFullscreenState();
    }

    @Inject
    public PresenterFactory mPresenterFactory;

    private StreamContract.Presenter mPresenter;

    private PlayerView mVideoView;
    private ExoPlayer mExoPlayer;
    private ImageButton mFullscreenToggle;
    private ImageButton mPlayButton;
    private ImageButton mQualityMenuButton;
    private ImageButton mFollowUnfollowButton;
    private PopupMenu mResolutionsMenu;
    private ProgressBar mProgressBar;
    private TextView mTitleTextView;
    private TextView mViewerCountTextView;

    private StreamActivityCallbacks mActivityCallbacks;

    private Stream mCurrentStream;

    @Inject
    public StreamFragment(){

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivityCallbacks = (StreamActivityCallbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = ViewModelProviders.of(this, mPresenterFactory)
                .get(StreamPresenter.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stream, container, false);
        mVideoView = fragmentView.findViewById(R.id.stream_player);
        if (mExoPlayer == null)
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity());
        mVideoView.setPlayer(mExoPlayer);
        mVideoView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

        mPlayButton = fragmentView.findViewById(R.id.exo_play);
        mPlayButton.setOnClickListener(v -> {
            if (mExoPlayer != null) {
                mExoPlayer.setPlayWhenReady(true);
            }
        });

        mFullscreenToggle = fragmentView.findViewById(R.id.fullscreen_toggle);
        mFullscreenToggle.setOnClickListener(v ->
                mActivityCallbacks.toggleFullscreen(!mActivityCallbacks.getFullscreenState()));

        mFollowUnfollowButton = fragmentView.findViewById(R.id.follow_unfollow);
        mFollowUnfollowButton.setOnClickListener(v -> mPresenter.followOrUnfollowChannel());
        mFollowUnfollowButton.setEnabled(false);

        mQualityMenuButton = fragmentView.findViewById(R.id.qualities_menu);
        mQualityMenuButton.setOnClickListener(v -> {
            if (mResolutionsMenu != null) {
                mResolutionsMenu.show();
            }
        });

        mTitleTextView = fragmentView.findViewById(R.id.text_view_stream_title);
        mViewerCountTextView = fragmentView.findViewById(R.id.text_view_stream_viewers);

        mProgressBar = fragmentView.findViewById(R.id.stream_buffer_playerview_progressbar);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.subscribe(this, this.getLifecycle(), mCurrentStream);
        mVideoView.onResume();
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mVideoView.onPause();
        mExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVideoView = null;
        mExoPlayer.release();
        mExoPlayer = null;
        mFullscreenToggle = null;
        mQualityMenuButton = null;
        mTitleTextView = null;
        mViewerCountTextView = null;
        mProgressBar = null;
        mPlayButton = null;
        mResolutionsMenu = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityCallbacks = null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Quality quality = Quality.values()[item.getItemId()];
        mPresenter.playChosenQuality(quality);
        return true;
    }

    @Override
    public void displayStream(String url) {
        if (url == null || url.isEmpty()) {
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
    public void displayInfoMessage(InfoMessage message, String streamerName) {
        String infoMessage =
                getResources().getStringArray(R.array.stream_info_messages)[message.ordinal()];
        if (message == InfoMessage.INFO_CHANNEL_UNFOLLOWED ||
            message == InfoMessage.INFO_CHANNEL_FOLLOWED) {
            infoMessage = infoMessage.concat(" " + streamerName);
        }
        Toast.makeText(getActivity(), infoMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

    @Override
    public void setQualitiesMenu(List<Quality> qualities) {
        if (isAdded()) {
            mResolutionsMenu = new PopupMenu(getActivity(), mQualityMenuButton);
            for (Quality quality : qualities)
                mResolutionsMenu.getMenu().add(
                        1,
                        quality.ordinal(),
                        0,
                        quality.getQualityShortName(getActivity()));
            mResolutionsMenu.setOnMenuItemClickListener(this);
        }
    }

    @Override
    public void displayLoading(boolean loadingState) {
        mProgressBar.setIndeterminate(loadingState);
        if (loadingState) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void displayTitle(String title) {
        if (mTitleTextView != null) mTitleTextView.setText(title);
    }

    @Override
    public void displayViewerCount(String count) {
        if (mViewerCountTextView != null) mViewerCountTextView.setText(count);
    }

    @Override
    public void displayFollowStatus(boolean followed) {
        if (followed) {
            mFollowUnfollowButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white));
        } else {
            mFollowUnfollowButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_white));
        }
    }

    @Override
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

    @Override
    public void enableFollow(boolean enabled) {
        mFollowUnfollowButton.setEnabled(enabled);
    }

    public void setCurrentStream(Stream stream) {
        mCurrentStream = stream;
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