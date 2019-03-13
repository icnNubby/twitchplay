package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dagger.android.support.DaggerFragment;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.presentation.stream.StreamChatActivity;

public class StreamListFragment extends DaggerFragment implements StreamListContract.View {
    private final String TAG = StreamListFragment.class.getSimpleName();
    private final String BUNDLE_NAVBAR_STATE = "navbar_state";
    private final String BUNDLE_TIME_STATE = "paused_at";

    private Picasso mPicasso; // TODO Inject?

    private RecyclerView mStreamListRecyclerView;

    @Inject
    StreamListContract.Presenter mPresenter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private float mStreamCardWidth;
    private float mStreamCardHeight;
    private float mDensity;

    private int mPreviewSize; //1 - big, 2 - small.

    private long mPausedAt;
    private boolean mFirstLoad = true;


    @Inject
    public StreamListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPicasso = Picasso.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stream_list, container, false);
        mStreamListRecyclerView = fragmentView.findViewById(R.id.stream_list_recycler_view);
        mStreamListRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        mSwipeRefreshLayout = fragmentView.findViewById(R.id.stream_list_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.updateStreams());
        mProgressBar = fragmentView.findViewById(R.id.stream_list_progress_bar);
        mDensity = getActivity().getResources().getDisplayMetrics().density;
        fragmentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            mStreamCardWidth = mStreamListRecyclerView.getMeasuredWidth() / mDensity;
            mStreamCardHeight = mStreamCardWidth * 9 / 16;
            ((GridLayoutManager) mStreamListRecyclerView.getLayoutManager())
                    .setSpanCount((int) mStreamCardWidth / 250);
        });
        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPausedAt = savedInstanceState.getLong(BUNDLE_TIME_STATE);
            mFirstLoad = false;
        }
        if (mPausedAt > 0) decideToUpdate(mPausedAt);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_TIME_STATE, SystemClock.elapsedRealtime());
        mPausedAt = SystemClock.elapsedRealtime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unsubscribe();
    }

    @Override
    public void displayStreamList(List<Stream> streams) {
        StreamListAdapter streamListAdapter = new StreamListAdapter(streams);
        if (mStreamListRecyclerView != null) {
            mStreamListRecyclerView.setAdapter(streamListAdapter);
            streamListAdapter.notifyDataSetChanged();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void clearStreamList() {
        StreamListAdapter streamListAdapter = new StreamListAdapter(new ArrayList<>());
        if (mStreamListRecyclerView != null) {
            mStreamListRecyclerView.setAdapter(streamListAdapter);
            streamListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void addStreamList(List<Stream> streams) {
        if (mStreamListRecyclerView != null) {
            StreamListAdapter streamListAdapter = (StreamListAdapter)
                    mStreamListRecyclerView.getAdapter();
            if (streamListAdapter != null) {
                int sizeBefore = streamListAdapter.getItemCount();
                streamListAdapter.notifyItemRangeInserted(sizeBefore, streams.size());
            }
        }
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

    public StreamListContract.Presenter returnAttachedPresenter() { //todo remove that
        return mPresenter;
    }

    public void setupProgressBar(boolean visible) {
        if (mProgressBar != null) {
            if (visible) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
            mProgressBar.setIndeterminate(visible);
        }
    }

    @Override
    public void displayError(ErrorMessage message) {
        mSwipeRefreshLayout.setRefreshing(false);
        String errorMessage =
                getResources().getStringArray(R.array.streams_list_errors)[message.ordinal()];
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPreviewSize(int size) {
        mPreviewSize = size;
    }

    private void decideToUpdate(long savedTime) {
        if (mPresenter != null) {
            mPresenter.decideToReload(SystemClock.elapsedRealtime() - savedTime);
        }
    }


    private class StreamListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Stream mStream;
        private TextView mTextViewStreamDescription;
        private TextView mTextViewStreamerName;
        private TextView mTextViewStreamViewerCount;
        private ImageView mStreamPreview;
        private ImageView mAvatar;

        public void bind(Stream stream) {
            mStream = stream;
            mTextViewStreamerName.setText(stream.getStreamerName());
            mTextViewStreamDescription.setText(stream.getTitle());
            mTextViewStreamViewerCount.setText(stream.getViewerCount());
            String formattedUrl = stream
                    .getThumbnailUrl()
                    .replace("{width}", "" + (int) (mStreamCardWidth))
                    .replace("{height}", "" + (int) (mStreamCardHeight));
            mPicasso // todo inject
                    .load(formattedUrl)
                    .placeholder(R.drawable.video_placeholder)
                    .resize((int) (mStreamCardWidth * mDensity), (int) (mStreamCardHeight * mDensity))
                    .into(mStreamPreview);
            if (stream.getUserData() != null) {
                mPicasso.load(stream.getUserData().getProfileImageUrl())
                        .into(mAvatar);
            }
        }

        public StreamListViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewStreamDescription = itemView.findViewById(R.id.stream_description);
            mTextViewStreamerName = itemView.findViewById(R.id.stream_streamer_name);
            mStreamPreview = itemView.findViewById(R.id.stream_preview_thumbnail);
            mTextViewStreamViewerCount = itemView.findViewById(R.id.stream_viewer_count);
            mAvatar = itemView.findViewById(R.id.imageViewAvatar);
        }

        @Override
        public void onClick(View v) {
            Intent startStream = new Intent(getContext(), StreamChatActivity.class);
            startStream.putExtra("stream_json", new Gson().toJson(mStream)); // SLOW, YES
            startActivity(startStream);
        }
    }

    private class StreamListAdapter extends RecyclerView.Adapter<StreamListViewHolder> {
        private List<Stream> mStreamsList;

        public StreamListAdapter(List<Stream> streams) {
            mStreamsList = streams;
        }

        @NonNull
        @Override
        public StreamListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int previewId;
            switch (mPreviewSize) {
                case 0: {
                    previewId = R.layout.stream_list_element_big;
                    break;
                }
                case 1: {
                    previewId = R.layout.stream_list_element_small;
                    break;
                }
                default: {
                    previewId = R.layout.stream_list_element_small;
                }
            }
            View view = LayoutInflater
                    .from(getActivity())
                    .inflate(previewId, parent, false);
            StreamListViewHolder listViewHolder = new StreamListViewHolder(view);
            view.setOnClickListener(listViewHolder);
            return listViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull StreamListViewHolder holder, int position) {
            holder.bind(mStreamsList.get(position));
            if (position == mStreamsList.size() - 3) {
                mPresenter.getMoreStreams();
            }
        }

        @Override
        public int getItemCount() {
            return mStreamsList.size();
        }

    }
}
