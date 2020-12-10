package com.quidvis.moneydrop.utility;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.interfaces.OnAwesomeDialogClickListener;

/**
 * Created by Wisdom Emenike.
 * Date: 4/15/2019
 * Time: 4:34 PM
 */

public class AwesomeAlertDialog extends Dialog {

    private final TextView dialogTitle;
    private final TextView dialogMessage;
    private final Button dialogBtnPositive;
    private final Button dialogBtnNegative;
    private final LinearLayout dialogView;

    public AwesomeAlertDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.awesome_alert_dialog);
        dialogView = findViewById(R.id.awesome_dialog_view);
        dialogTitle = findViewById(R.id.dialog_title);
        dialogMessage = findViewById(R.id.dialog_message);
        dialogBtnPositive = findViewById(R.id.btn_positive);
        dialogBtnNegative = findViewById(R.id.btn_negative);
        Window window = getWindow();
        assert window != null;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        window.getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public TextView getDialogTitle() {
        return dialogTitle;
    }

    public TextView getDialogMessage() {
        return dialogMessage;
    }

    public Button getDialogBtnPositive() {
        return dialogBtnPositive;
    }

    public Button getDialogBtnNegative() {
        return dialogBtnNegative;
    }

    public LinearLayout getDialogView() {
        return dialogView;
    }

    public void setIcon(Drawable drawable) {
        dialogTitle.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
    }

    public void setTitle(@Nullable CharSequence title) {
        dialogTitle.setVisibility(View.VISIBLE);
        dialogTitle.setText(title);
    }

    public void setMessage(@Nullable CharSequence message) {
        dialogMessage.setVisibility(View.VISIBLE);
        dialogMessage.setText(message);
    }

    public void setCancelable(boolean cancelable) {
        setCanceledOnTouchOutside(cancelable);
    }

    public void setPositiveButton(CharSequence text) {
        setPositiveButton(text, null);
    }

    public void setPositiveButton(CharSequence text, final OnAwesomeDialogClickListener listener) {
        dialogBtnPositive.setVisibility(View.VISIBLE);
        dialogBtnPositive.setText(text);

        View.OnClickListener onClickListener = v -> {
            if (listener != null)
                listener.onClick(AwesomeAlertDialog.this);
            else detach();
        };

        dialogBtnPositive.setOnClickListener(onClickListener);
    }

    public void setNegativeButton(CharSequence text) {
        setNegativeButton(text, null);
    }

    public void setNegativeButton(CharSequence text, final OnAwesomeDialogClickListener listener) {
        dialogBtnNegative.setVisibility(View.VISIBLE);
        dialogBtnNegative.setText(text);

        View.OnClickListener onClickListener = v -> {
            if (listener != null)
                listener.onClick(AwesomeAlertDialog.this);
            else detach();
        };

        dialogBtnNegative.setOnClickListener(onClickListener);
    }

    public void setView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        dialogView.addView(view);
    }

    public void display() {
        show();
    }

    private void detach() {
        dismiss();
    }
}
