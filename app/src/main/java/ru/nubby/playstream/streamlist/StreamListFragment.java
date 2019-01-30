package ru.nubby.playstream.streamlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.stream.StreamActivity;

public class StreamListFragment extends Fragment implements StreamListContract.View {

    private RecyclerView mStreamListRecyclerView;
    private StreamListContract.Presenter mPresenter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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
        mStreamListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout = fragmentView.findViewById(R.id.stream_list_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.addMoreStreams());
        mPresenter.addMoreStreams();
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void setPresenter(@NonNull StreamListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private class StreamListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Stream mStream;
        private TextView mTextViewStreamDescription;

        public void bind(Stream stream) {
            mStream = stream;
            mTextViewStreamDescription.setText(stream.getStreamerName() + "___"  + stream.getTitle());
        }

        public StreamListViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewStreamDescription = itemView.findViewById(R.id.stream_description);
        }

        @Override
        public void onClick(View v) {
            Intent startStream = new Intent(getContext(), StreamActivity.class);
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
        }

        @Override
        public int getItemCount() {
            return mStreamsList.size();
        }
    }
}
