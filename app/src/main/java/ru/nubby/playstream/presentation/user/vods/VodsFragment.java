package ru.nubby.playstream.presentation.user.vods;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.nubby.playstream.R;
import ru.nubby.playstream.di.scopes.ActivityScope;

@ActivityScope
public class VodsFragment extends Fragment {

    private RecyclerView mPanelsRecyclerView;
    private TextView mEmptyPanelsStub;

    @Inject
    public VodsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_vods, container, false);
        mEmptyPanelsStub = view.findViewById(R.id.vods_empty_stub);
        mEmptyPanelsStub.setVisibility(View.GONE);
        mPanelsRecyclerView = view.findViewById(R.id.vods_recycler_view);
        mPanelsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {
        mPanelsRecyclerView.setAdapter(new VodsFragment.FakePageAdapter(20));
    }

    private static class FakePageAdapter extends RecyclerView.Adapter<FakePageVH> {
        private final int numItems;

        FakePageAdapter(int numItems) {
            this.numItems = numItems;
        }

        @Override
        public FakePageVH onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.vods_list_element, viewGroup, false);

            return new FakePageVH(itemView);
        }

        @Override
        public void onBindViewHolder(FakePageVH fakePageVH, int i) {
            // do nothing
        }

        @Override
        public int getItemCount() {
            return numItems;
        }
    }

    private static class FakePageVH extends RecyclerView.ViewHolder {
        FakePageVH(View itemView) {
            super(itemView);
        }
    }
}
