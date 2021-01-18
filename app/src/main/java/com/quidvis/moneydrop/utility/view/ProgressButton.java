package com.quidvis.moneydrop.utility.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.utility.Utility;

public class ProgressButton extends RelativeLayout {

    private String btnText;
    private Button button;
    private ProgressBar progressBar;

    public ProgressButton(Context context) {
        super(context);
        setUp(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context, attrs);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context, attrs);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context, attrs);
    }

    public void startProgress() {
        button.setText("");
        progressBar.setVisibility(VISIBLE);
    }

    public void stopProgress() {
        progressBar.setVisibility(GONE);
        button.setText(btnText);
    }

    public void setButtonOnClickListener(@Nullable OnClickListener l) {
        button.setOnClickListener(v -> {
            if (l != null) l.onClick(ProgressButton.this);
        });
    }

    public void setBtnText(String text) {
        getButton().setText(btnText = text);
    }

    private void setUp(Context context, AttributeSet attrs) {

        if (attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressButton, 0, 0);

            button = getButton(a.getResourceId(R.styleable.ProgressButton_btnStyle, 0),
                    a.getLayoutDimension(R.styleable.ProgressButton_btnWidth, LayoutParams.MATCH_PARENT),
                    a.getLayoutDimension(R.styleable.ProgressButton_btnHeight, LayoutParams.WRAP_CONTENT),
                    a.getDimensionPixelSize(R.styleable.ProgressButton_btnPadding, 0));

            progressBar = getProgressBar();

            button.setText(btnText = a.getString(R.styleable.ProgressButton_btnText));
            int textSize = a.getDimensionPixelSize(R.styleable.ProgressButton_btnTextSize, 0);
            if (textSize != 0) button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) textSize);
            int color = a.getResourceId(R.styleable.ProgressButton_btnTextColor, 0);
            if (color != 0) button.setTextColor(getResources().getColor(color));
            button.setTypeface(button.getTypeface(), a.getInt(R.styleable.ProgressButton_btnTextStyle, Typeface.NORMAL));
            button.setAllCaps(a.getBoolean(R.styleable.ProgressButton_btnTextAllCaps, false));
            int background = a.getResourceId(R.styleable.ProgressButton_btnBackground, 0);
            if (background != 0) button.setBackgroundResource(background);
            int backgroundTint = a.getResourceId(R.styleable.ProgressButton_btnBackgroundTint, 0);
            if (backgroundTint != 0) button.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), backgroundTint));

            int progressBarColor = a.getResourceId(R.styleable.ProgressButton_progressBarColor, 0);
            if (progressBarColor != 0) progressBar.setIndeterminateTintList(AppCompatResources.getColorStateList(getContext(), progressBarColor));

        } else {
            button = getButton();
            progressBar = getProgressBar();
        }

        addView(button);
        addView(progressBar);
    }

    public Button getButton() {
        return button != null ? button : getButton(0, 0, 0, 0);
    }

    private Button getButton(int style, int width, int height, int padding) {
        Button button = style == 0 ? new Button(getContext()) : new Button(new ContextThemeWrapper(getContext(), style), null, style);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        if (padding != 0) button.setPadding(padding, padding, padding, padding);
        button.setLayoutParams(layoutParams);
        return button;
    }

    private ProgressBar getProgressBar() {
        ProgressBar progressBar = new ProgressBar(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.setElevation((float) getDip(5));
        int padding = getDip(10);
        progressBar.setPadding(padding, padding, padding, padding);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(GONE);
        return progressBar;
    }

    private int getDip(int dip) {
        return Utility.getDip((Activity) getContext(), dip);
    }
}
