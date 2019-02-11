package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.ui.stream.StreamChatActivity;

public class StreamListFragment extends Fragment implements StreamListContract.View {
    private final String TAG = "StreamListFragment";

    private RecyclerView mStreamListRecyclerView;
    private StreamListContract.Presenter mPresenter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private float streamCardWidth;
    private float streamCardHeight;
    private float density;

    public static StreamListFragment newInstance() {

        Bundle args = new Bundle();

        StreamListFragment fragment = new StreamListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stream_list, container, false);
        mStreamListRecyclerView = fragmentView.findViewById(R.id.stream_list_recycler_view);
        mStreamListRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        mSwipeRefreshLayout = fragmentView.findViewById(R.id.stream_list_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.updateStreams());
        density = getActivity().getResources().getDisplayMetrics().density;
        fragmentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            streamCardWidth = mStreamListRecyclerView.getMeasuredWidth() / density ;
            streamCardHeight = streamCardWidth * 9 / 16;
            ((GridLayoutManager) mStreamListRecyclerView.getLayoutManager()).setSpanCount((int) streamCardWidth / 250);
        });
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mStreamListRecyclerView = null;
        mSwipeRefreshLayout = null;
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
    public void addStreamList(List<Stream> streams) {
        if (mStreamListRecyclerView != null) {
            StreamListAdapter streamListAdapter = (StreamListAdapter) mStreamListRecyclerView.getAdapter();
            if (streamListAdapter != null) {
                int sizeBefore = streamListAdapter.getItemCount();
                streamListAdapter.addStreams(streams);
                streamListAdapter.notifyItemRangeInserted(sizeBefore, streams.size());
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setPresenter(@NonNull StreamListContract.Presenter fragmentPresenter) {
        mPresenter = fragmentPresenter;
    }

    private class StreamListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Stream mStream;
        private TextView mTextViewStreamDescription;
        private TextView mTextViewStreamerName;
        private ImageView mStreamPreview;

        public void bind(Stream stream) {
            mStream = stream;
            mTextViewStreamerName.setText(stream.getStreamerName());
            mTextViewStreamDescription.setText(stream.getTitle());
            String formattedUrl = stream
                    .getThumbnailUrl()
                    .replace("{width}", "" + (int) (streamCardWidth))
                    .replace("{height}", "" + (int) (streamCardHeight));
            Log.d(TAG, formattedUrl);
            Picasso.get() // todo inject
                    .load(formattedUrl)
                    .placeholder(R.drawable.video_placeholder)
                    .resize((int)(streamCardWidth * density), (int)(streamCardHeight * density))
                    .into(mStreamPreview);
        }

        public StreamListViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewStreamDescription = itemView.findViewById(R.id.stream_description);
            mTextViewStreamerName = itemView.findViewById(R.id.stream_streamer_name);
            mStreamPreview = itemView.findViewById(R.id.stream_preview_thumbnail);
        }

        @Override
        public void onClick(View v) {
            Intent startStream = new Intent(getContext(), StreamChatActivity.class);
            //TODO fix to some constant
            startStream.putExtra("stream_json", new Gson().toJson(mStream)); // SLOW, YES
            startActivity(startStream );
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
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.stream_list_element, parent, false);
            StreamListViewHolder listViewHolder = new StreamListViewHolder(view);
            view.setOnClickListener(listViewHolder);
            return listViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull StreamListViewHolder holder, int position) {
            holder.bind(mStreamsList.get(position));
            if (position == mStreamsList.size() - 3) {
                mSwipeRefreshLayout.setRefreshing(true);
                mPresenter.addMoreStreams();
            }
        }

        @Override
        public int getItemCount() {
            return mStreamsList.size();
        }

        public void addStreams(List<Stream> streams) {
            mStreamsList.addAll(streams);
        }
    }
}
