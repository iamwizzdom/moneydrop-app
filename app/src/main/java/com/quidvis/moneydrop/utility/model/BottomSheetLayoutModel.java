package com.quidvis.moneydrop.utility.model;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.quidvis.moneydrop.utility.CustomBottomSheet;

public class BottomSheetLayoutModel {

    private Drawable icon;
    private String text;
    private OnClickListener onClickListener;

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
