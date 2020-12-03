package com.quidvis.moneydrop.utility.model;

import android.graphics.drawable.Drawable;
import android.view.View;

public class BottomSheetLayoutModel {

    private Drawable icon;
    private String text;
    private View.OnClickListener onClickListener;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable drawable) {
        this.icon = drawable;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
