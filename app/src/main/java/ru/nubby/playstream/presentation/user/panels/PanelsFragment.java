package ru.nubby.playstream.presentation.user.panels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.nubby.playstream.R;
import ru.nubby.playstream.di.scopes.ActivityScope;

@ActivityScope
public class PanelsFragment extends Fragment {

    private RecyclerView mPanelsRecyclerView;

    @Inject
    public PanelsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPanelsRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_user_panels, container, false);
        mPanelsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mPanelsRecyclerView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {
        mPanelsRecyclerView.setAdapter(new FakePageAdapter(20));
    }



    private static class FakePageAdapter extends RecyclerView.Adapter<FakePageVH> {
        private final int numItems;

        FakePageAdapter(int numItems) {
            this.numItems = numItems;
        }

        @Override
        public FakePageVH onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.panels_list_element, viewGroup, false);

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
