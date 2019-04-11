package ru.nubby.playstream.presentation.user.panels;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.nubby.playstream.R;
import ru.nubby.playstream.di.scopes.ActivityScope;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.ChannelPanelAdditionalData;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.presentation.base.PresenterFactory;

@ActivityScope
public class PanelsFragment extends Fragment implements PanelsContract.View {

    @Inject
    public PresenterFactory mPresenterFactory;

    private PanelsContract.Presenter mPresenter;
    private UserData mUser;

    private RecyclerView mPanelsRecyclerView;
    private TextView mEmptyPanelsStub;
    private Picasso mPicasso;

    @Inject
    public PanelsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = ViewModelProviders.of(this, mPresenterFactory).get(PanelsPresenter.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_panels, container, false);
        mEmptyPanelsStub = view.findViewById(R.id.panels_empty_stub);
        mPanelsRecyclerView = view.findViewById(R.id.panels_recycler_view);
        mPanelsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPicasso = Picasso.get();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.subscribe(this, this.getLifecycle(), mUser);
    }

    @Override
    public void displayStub() {
        mEmptyPanelsStub.setVisibility(View.VISIBLE);
        mPanelsRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void displayPanels(List<ChannelPanel> panels) {
        mEmptyPanelsStub.setVisibility(View.GONE);
        mPanelsRecyclerView.setVisibility(View.VISIBLE);
        mPanelsRecyclerView.setAdapter(new PanelsAdapter(panels));
    }

    @Override
    public void displayInfoMessage(ErrorMessage message) {
        String infoMessage =
                getResources().getStringArray(R.array.panels_list_errors)[message.ordinal()];
        Toast.makeText(getActivity(), infoMessage, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean hasPresenterAttached() {
        return false;
    }

    public void setUser(UserData user) {
        mUser = user;
    }

    private class PanelsAdapter extends RecyclerView.Adapter<PanelViewHolder> {
        private List<ChannelPanel> mChannelPanels;

        PanelsAdapter(List<ChannelPanel> panels) {
            mChannelPanels = panels;
        }

        @NonNull
        @Override
        public PanelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.panels_list_element, viewGroup, false);

            return new PanelViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PanelViewHolder panelViewHolder, int i) {
            panelViewHolder.bind(mChannelPanels.get(i));
        }

        @Override
        public int getItemCount() {
            return mChannelPanels.size();
        }
    }

    private class PanelViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPanelImage;
        private TextView mPanelHtml;

        PanelViewHolder(View itemView) {
            super(itemView);
            mPanelHtml = itemView.findViewById(R.id.panel_html);
            mPanelImage = itemView.findViewById(R.id.panel_image);
        }

        void bind(ChannelPanel panel) {
            setupImage(panel);
            setupHtmlView(panel);
        }

        private void setupImage(ChannelPanel panel) {
            ChannelPanelAdditionalData data = panel.getData();
            String link = "";
            String image = "";
            if (data != null) {
                if (data.getLink() != null && !data.getLink().isEmpty()) {
                    link = data.getLink();
                }
                if (data.getImage() != null && !data.getImage().isEmpty()) {
                    image = data.getImage();
                }
            }
            if (!image.isEmpty()) {
                mPanelImage.setVisibility(View.VISIBLE);
                mPicasso.load(image)
                        .into(mPanelImage);
                if (!link.isEmpty()) {
                    final String linkCopy = link;
                    mPanelImage.setOnClickListener(v -> {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(linkCopy));
                        startActivity(intent);
                    });
                }
            } else {
                mPanelImage.setVisibility(View.GONE);
            }
        }

        private void setupHtmlView(ChannelPanel panel) {
            String html = panel.getHtmlDescription();
            if (html != null && !html.isEmpty()) {
                mPanelHtml.setVisibility(View.VISIBLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mPanelHtml.setText(Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY));
                } else {
                    mPanelHtml.setText(Html.fromHtml(html));
                }
            } else {
                mPanelHtml.setVisibility(View.GONE);
            }
        }
    }

}
