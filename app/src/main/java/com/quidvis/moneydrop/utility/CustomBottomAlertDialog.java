package com.quidvis.moneydrop.utility;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quidvis.moneydrop.R;

/**
 * Created by Wisdom Emenike.
 * Date: 4/15/2019
 * Time: 4:34 PM
 */

public class CustomBottomAlertDialog {

    private final CustomBottomSheet bottomSheet;
    private final TextView dialogMessage;
    private final Button dialogBtnPositive;
    private final Button dialogBtnNegative;
    private final LinearLayout dialogView;

    public CustomBottomAlertDialog(@NonNull AppCompatActivity activity) {
        View view = activity.getLayoutInflater().inflate(R.layout.custom_bottom_alert_dialog, null);
        dialogView = view.findViewById(R.id.custom_dialog_view);
        dialogMessage = view.findViewById(R.id.dialog_message);
        dialogBtnPositive = view.findViewById(R.id.btn_positive);
        dialogBtnNegative = view.findViewById(R.id.btn_negative);
        bottomSheet = new CustomBottomSheet(activity, view);
        dialogBtnPositive.setOnClickListener(v -> bottomSheet.dismiss());
        dialogBtnNegative.setOnClickListener(v -> bottomSheet.dismiss());
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

    public void setMessage(@Nullable CharSequence message) {
        dialogMessage.setVisibility(View.VISIBLE);
        dialogMessage.setText(message);
    }

    public void setIcon(int resourceId) {
        dialogMessage.setCompoundDrawablesWithIntrinsicBounds(0, resourceId, 0,0);
    }

    public void setCancelable(boolean cancelable) {
        bottomSheet.setCancelable(cancelable);
    }

    public void setPositiveButton(CharSequence text) {
        setPositiveButton(text, (View.OnClickListener) null);
    }

    public void setPositiveButton(CharSequence text, final View.OnClickListener listener) {
        dialogBtnPositive.setVisibility(View.VISIBLE);
        dialogBtnPositive.setText(text);

        View.OnClickListener onClickListener = v -> {
            if (listener != null) listener.onClick(v);
            detach();
        };

        dialogBtnPositive.setOnClickListener(onClickListener);
    }

    public void setPositiveButton(CharSequence text, final OnClickListener listener) {
        dialogBtnPositive.setVisibility(View.VISIBLE);
        dialogBtnPositive.setText(text);

        View.OnClickListener onClickListener = v -> {
            if (listener != null) listener.onClick(v, dialogView);
            detach();
        };

        dialogBtnPositive.setOnClickListener(onClickListener);
    }

    public void setNegativeButton(CharSequence text) {
        setNegativeButton(text, (View.OnClickListener) null);
    }

    public void setNegativeButton(CharSequence text, final View.OnClickListener listener) {
        dialogBtnNegative.setVisibility(View.VISIBLE);
        dialogBtnNegative.setText(text);

        View.OnClickListener onClickListener = v -> {
            if (listener != null) listener.onClick(v);
            detach();
        };

        dialogBtnNegative.setOnClickListener(onClickListener);
    }

    public void setNegativeButton(CharSequence text, final OnClickListener listener) {
        dialogBtnNegative.setVisibility(View.VISIBLE);
        dialogBtnNegative.setText(text);

        View.OnClickListener onClickListener = v -> {
            if (listener != null) listener.onClick(v, dialogView);
            detach();
        };

        dialogBtnNegative.setOnClickListener(onClickListener);
    }

    public void addView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        dialogView.addView(view);
    }

    public void display() {
        bottomSheet.show();
    }

    private void detach() {
        bottomSheet.dismiss();
    }

    public interface OnClickListener {
        void onClick(View btnView, View dialogView);
    }
}
