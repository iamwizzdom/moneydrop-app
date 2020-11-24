package com.quidvis.moneydrop.utility;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.quidvis.moneydrop.R;

import java.util.Objects;

public class CustomBottomSheet extends BottomSheetDialogFragment {

    private final AppCompatActivity activity;
    private View innerView;

    public static CustomBottomSheet newInstance(AppCompatActivity activity) {
        return new CustomBottomSheet(activity);
    }

    public CustomBottomSheet(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_bottom_sheet, container, false);
        LinearLayout layout = view.findViewById(R.id.sheet_layout);
        if (innerView != null) layout.addView(innerView);
        //dialog cancel when touches outside (Optional)
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);
        return view;
    }

    public void setView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        this.innerView = view;
    }

    public void show() {
        this.show(this.activity.getSupportFragmentManager(), this.getTag());
    }

}
