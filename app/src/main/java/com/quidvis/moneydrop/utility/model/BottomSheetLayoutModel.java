package com.quidvis.moneydrop.utility.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.utility.CustomBottomSheet;

public class BottomSheetLayoutModel {

    private Drawable iconLeft, iconRight;
    private String text;
    private OnClickListener onClickListener;

    public Drawable getIconLeft() {
        return iconLeft;
    }

    public void setIconLeft(Drawable drawable) {
        this.iconLeft = drawable;
    }

    public void setIconLeft(int resource, Context context) {
        this.iconLeft = ResourcesCompat.getDrawable(context.getResources(), resource, null);
    }

    public Drawable getIconRight() {
        return iconRight;
    }

    public void setIconRight(Drawable iconRight) {
        this.iconRight = iconRight;
    }

    public void setIconRight(int resource, Context context) {
        this.iconRight = ResourcesCompat.getDrawable(context.getResources(), resource, null);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(CustomBottomSheet sheet, View v);
    }
}
