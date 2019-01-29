package ru.nubby.playstream.stream;

import android.app.ActionBar;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
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

    private StreamContract.Presenter mPresenter;
    private PlayerView mVideoView;
    private ExoPlayer mExoPlayer;


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
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity());
        mVideoView.setPlayer(mExoPlayer);
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
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.onPause();
        mExoPlayer.release();
    }




    @Override
    public void setPresenter(StreamContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
