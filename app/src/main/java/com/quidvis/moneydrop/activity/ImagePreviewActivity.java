package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.utility.RevealAnimation;

public class ImagePreviewActivity extends CustomCompatActivity {

    public static final String IMAGE_URL = "IMAGE_URL";

    private RevealAnimation mRevealAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        Intent intent = this.getIntent();   //get the intent to receive the x and y cords, that you passed before

        RelativeLayout rootLayout = findViewById(R.id.container); //there you have to get the root layout of your second activity
        mRevealAnimation = new RevealAnimation(rootLayout, intent, this);

        ImageView zoomableImage = findViewById(R.id.zoomableImage);

        if (intent.hasExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_BUNDLE)) {

            Bundle bundle = intent.getBundleExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_BUNDLE);

            if (bundle != null) {
                Glide.with(ImagePreviewActivity.this)
                        .load(bundle.getString(IMAGE_URL))
                        .placeholder(R.drawable.image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.image_placeholder)
                        .into(zoomableImage);
            }
        }
    }

    @Override
    public void onBackPressed() {
        mRevealAnimation.unRevealActivity();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}