package com.quidvis.moneydrop.utility;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.quidvis.moneydrop.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wisdom Emenike.
 * Date: 4/15/2019
 * Time: 4:34 PM
 */

public class CustomBottomAlertDialog {

    private final CustomBottomSheet bottomSheet;
    private CharSequence message, positiveBtnText, negativeBtnText;
    private View.OnClickListener positiveBtnListener, negativeBtnListener;
    private OnClickListener positiveBtnCustomListener, negativeBtnCustomListener;
    private int icon, lottieIcon;
    private final List<View> views = new ArrayList<>();
    private OnGotDialogViewListener onGotDialogViewListener;
    private LottieAnimationView lottieAnimationView;
    private TextView dialogMessage;
    private Button dialogBtnPositive, dialogBtnNegative;

    public CustomBottomAlertDialog(@NonNull AppCompatActivity activity) {
        bottomSheet = new CustomBottomSheet(activity, R.layout.custom_bottom_alert_dialog);
        bottomSheet.setOnViewInflatedListener(view -> {

            LinearLayout dialogView = view.findViewById(R.id.custom_dialog_view);
            lottieAnimationView = view.findViewById(R.id.anim_layout);
            dialogMessage = view.findViewById(R.id.dialog_message);
            dialogBtnPositive = view.findViewById(R.id.btn_positive);
            dialogBtnNegative = view.findViewById(R.id.btn_negative);

            if (message != null) {
                dialogMessage.setVisibility(View.VISIBLE);
                dialogMessage.setText(message);
            }

            if (icon != 0) {
                dialogMessage.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0,0);
                lottieAnimationView.setVisibility(View.GONE);
            } else if (lottieIcon != 0) {
                lottieAnimationView.setAnimation(lottieIcon);
                lottieAnimationView.setVisibility(View.VISIBLE);
            }

            if (positiveBtnText != null) {

                dialogBtnPositive.setVisibility(View.VISIBLE);
                dialogBtnPositive.setText(positiveBtnText);

                View.OnClickListener onClickListener = v -> {
                    if (positiveBtnListener != null) positiveBtnListener.onClick(v);
                    else if (positiveBtnCustomListener != null) positiveBtnCustomListener.onClick(v, dialogView);
                    detach();
                };

                dialogBtnPositive.setOnClickListener(onClickListener);

            } else {
                dialogBtnPositive.setOnClickListener(v -> bottomSheet.dismiss());
            }

            if (negativeBtnText != null) {

                dialogBtnNegative.setVisibility(View.VISIBLE);
                dialogBtnNegative.setText(negativeBtnText);

                View.OnClickListener onClickListener = v -> {
                    if (negativeBtnListener != null) negativeBtnListener.onClick(v);
                    else if (negativeBtnCustomListener != null) negativeBtnCustomListener.onClick(v, dialogView);
                    detach();
                };

                dialogBtnNegative.setOnClickListener(onClickListener);

            } else {
                dialogBtnNegative.setOnClickListener(v -> bottomSheet.dismiss());
            }

            if (this.views.size() > 0) {
                for (View v: this.views) dialogView.addView(v);
                this.views.clear();
            }

            if (this.onGotDialogViewListener != null) onGotDialogViewListener.onGotView(dialogView);

        });
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

    public void setOnGotDialogViewListener(OnGotDialogViewListener onGotDialogViewListener) {
        this.onGotDialogViewListener = onGotDialogViewListener;
    }

    public void setMessage(@Nullable CharSequence message) {
        this.message = message;
    }

    public void setIcon(int resourceId) {
        icon = resourceId;
    }

    public void setLottieIcon(int lottieIcon) {
        this.lottieIcon = lottieIcon;
    }

    public void setCancelable(boolean cancelable) {
        bottomSheet.setCancelable(cancelable);
    }

    public void setPositiveButton(CharSequence text) {
        setPositiveButton(text, (View.OnClickListener) null);
    }

    public void setPositiveButton(CharSequence text, final View.OnClickListener listener) {
        positiveBtnText = text;
        positiveBtnListener = listener;
    }

    public void setPositiveButton(CharSequence text, final OnClickListener listener) {
        positiveBtnText = text;
        positiveBtnCustomListener = listener;
    }

    public void setNegativeButton(CharSequence text) {
        setNegativeButton(text, (View.OnClickListener) null);
    }

    public void setNegativeButton(CharSequence text, final View.OnClickListener listener) {
        negativeBtnText = text;
        negativeBtnListener = listener;
    }

    public void setNegativeButton(CharSequence text, final OnClickListener listener) {
        negativeBtnText = text;
        negativeBtnCustomListener = listener;
    }

    public void addView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        this.views.add(view);
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

    public interface OnGotDialogViewListener {
        void onGotView(View view);
    }
}
