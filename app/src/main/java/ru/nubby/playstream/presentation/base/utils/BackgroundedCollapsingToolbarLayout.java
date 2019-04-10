package ru.nubby.playstream.presentation.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class BackgroundedCollapsingToolbarLayout extends CollapsingToolbarLayout implements Target {

    public BackgroundedCollapsingToolbarLayout(Context context) {
        super(context);
    }

    public BackgroundedCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        bitmapDrawable.setGravity(Gravity.CENTER);
        bitmapDrawable.setGravity(Gravity.DISPLAY_CLIP_VERTICAL);
        setBackground(bitmapDrawable);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        setBackground(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        setBackground(placeHolderDrawable);
    }
}
