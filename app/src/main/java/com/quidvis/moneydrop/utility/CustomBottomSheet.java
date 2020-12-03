package com.quidvis.moneydrop.utility;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;

import java.util.ArrayList;
import java.util.Objects;

public class CustomBottomSheet extends BottomSheetDialogFragment {

    private final AppCompatActivity activity;
    private final ArrayList<BottomSheetLayoutModel> layoutModels;
    private View innerView;

    public static CustomBottomSheet newInstance(AppCompatActivity activity) {
        return new CustomBottomSheet(activity);
    }

    public static CustomBottomSheet newInstance(AppCompatActivity activity,
                                                ArrayList<BottomSheetLayoutModel> layoutModels) {
        return new CustomBottomSheet(activity, layoutModels);
    }

    public CustomBottomSheet(AppCompatActivity activity) {
        this.activity = activity;
        this.layoutModels = null;
    }

    public CustomBottomSheet(AppCompatActivity activity, ArrayList<BottomSheetLayoutModel> layoutModels) {
        this.activity = activity;
        this.layoutModels = layoutModels;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout layout = view.findViewById(R.id.sheet_layout);
        if (innerView != null) layout.addView(innerView);
        else if (this.layoutModels != null) {

            int size = this.layoutModels.size();

            for (int i = 0; i < size; i++) {
                BottomSheetLayoutModel layoutModel = this.layoutModels.get(i);
                LinearLayout linearLayout = getView(layoutModel);
                if (size > 1 && i < (size - 1)) linearLayout.setBackground(
                        ResourcesCompat.getDrawable(activity.getResources(),
                                R.drawable.layout_underline, null)
                );
                layout.addView(linearLayout);
            }

        }
        //dialog cancel when touches outside (Optional)
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);
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

    private LinearLayout getView(BottomSheetLayoutModel layoutModel) {

        LinearLayout layout = new LinearLayout(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);

        int padding = Utility.getDip(activity, 20);
        int paddingSides = Utility.getDip(activity, 10);
        layout.setPadding(paddingSides, padding, paddingSides, padding);
        layout.addView(getTextView(layoutModel));

        if (layoutModel.getOnClickListener() != null) layout.setOnClickListener(layoutModel.getOnClickListener());

        return layout;
    }

    private TextView getTextView(BottomSheetLayoutModel layoutModel) {
        TextView tv = new TextView(activity);

        tv.setTextAppearance(activity, R.style.text_view_style);
        tv.setTextColor(activity.getResources().getColor(R.color.titleColorGray));
        Typeface typeface = ResourcesCompat.getFont(activity, R.font.campton_light);
        tv.setTypeface(typeface, Typeface.BOLD);

        tv.setGravity(Gravity.START|Gravity.CENTER);
        tv.setCompoundDrawablesWithIntrinsicBounds(layoutModel.getIcon(), null, null, null);
        tv.setCompoundDrawablePadding(Utility.getDip(activity, 20));
        tv.setText(layoutModel.getText());

        return tv;
    }

}
