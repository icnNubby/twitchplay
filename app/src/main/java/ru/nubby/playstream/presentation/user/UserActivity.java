package ru.nubby.playstream.presentation.user;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import ru.nubby.playstream.R;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.presentation.base.BaseActivity;
import ru.nubby.playstream.presentation.base.PresenterFactory;
import ru.nubby.playstream.presentation.base.utils.BackgroundedCollapsingToolbarLayout;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamContract;
import ru.nubby.playstream.presentation.user.panels.PanelsFragment;
import ru.nubby.playstream.presentation.user.vods.VodsFragment;
import ru.nubby.playstream.utils.Constants;

public class UserActivity extends BaseActivity implements UserContract.View,
        AppBarLayout.OnOffsetChangedListener {

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;

    private UserContract.Presenter mPresenter;
    private UserData mUser;
    private Picasso mPicasso;

    private ImageView mUserAvatar;

    @Inject
    PresenterFactory mPresenterFactory;

    @Inject
    Gson mGson;

    @Inject
    PanelsFragment mPanelsFragment;

    @Inject
    VodsFragment mVodsFragment;

    private int mMaxScrollSize;
    private BackgroundedCollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private TextView mFollowers;
    private TextView mViews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mPresenter = ViewModelProviders.of(this, mPresenterFactory).get(UserPresenter.class);
        mUser = readUserDataFromExtras();
        mUserAvatar = findViewById(R.id.user_image);
        mPicasso = Picasso.get();

        ViewPager viewPager = findViewById(R.id.user_contents);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        mAppBarLayout = findViewById(R.id.user_appbar);
        mCollapsingToolbarLayout = findViewById(R.id.user_collapsing_toolbar);
        mToolbar = findViewById(R.id.streamer_info_toolbar);

        mFollowers = findViewById(R.id.user_followers);
        mViews = findViewById(R.id.user_views);

        mAppBarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = mAppBarLayout.getTotalScrollRange();
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.subscribe(this, this.getLifecycle(), mUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void displayUser(UserData user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            mPicasso.load(user.getProfileImageUrl())
                    .into(mUserAvatar);
        }
        mViews.setText(getString(R.string.user_views_label, user.getViewCount()));
    }

    @Override
    public void setupBackground(String url) {
        mPicasso.load(url)
                .placeholder(R.drawable.banner_placeholder)
                .into(mCollapsingToolbarLayout);
    }

    @Override
    public void displayFollowersCount(int followers) {
        mFollowers.setText(getString(R.string.user_follows_label, followers));
    }

    @Override
    public void displayFollowStatus(boolean followed) {
        //TODO
    }

    @Override
    public void displayInfoMessage(StreamContract.View.InfoMessage message, String streamerName) {
        //TODO
    }

    @Override
    public void enableFollow(boolean enabled) {
        //TODO
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            mUserAvatar.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(300)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            mUserAvatar.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        private static final int TAB_COUNT = 2;

        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int i) {
            //todo fix after tests
            switch (i) {
                case 0:
                    return mPanelsFragment;
                case 1:
                    return mVodsFragment;
                default:
                    return mPanelsFragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Tab " + String.valueOf(position);
        }
    }

    private UserData readUserDataFromExtras() {
        String jsonUser = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonUser = extras.getString(Constants.sUserIntentKey);
        }
        if (jsonUser == null) {
            Toast.makeText(this, getText(R.string.error_no_stream_info_provided), Toast.LENGTH_SHORT).show();
            finish();
        }
        UserData currentUser = mGson.fromJson(jsonUser, UserData.class);
        if (currentUser == null) {
            finish();
        }
        return currentUser;
    }
}
