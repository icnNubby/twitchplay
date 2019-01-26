package ru.nubby.playstream.stream;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ru.nubby.playstream.R;

public class StreamFragment extends Fragment implements StreamContract.View {
    private StreamContract.Presenter mPresenter;


    public static StreamFragment newInstance() {

        Bundle args = new Bundle();

        StreamFragment fragment = new StreamFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stream_list, container, false);
        return fragmentView;
    }

    @Override
    public void displayStream(String url) {
        Log.i("StreamFragment", "Fetched " + url);
    }



    @Override
    public void setPresenter(StreamContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
