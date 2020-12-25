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
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.interfaces.OnViewPagerMountedListener;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;

import java.util.ArrayList;
import java.util.Objects;

public class CustomBottomSheet extends BottomSheetDialogFragment {

    private final AppCompatActivity activity;
    private final ArrayList<BottomSheetLayoutModel> layoutModels;
    private final FragmentStatePagerAdapter pagerAdapter;
    private final View view;
    private String title;
    private OnViewPagerMountedListener onViewPagerMountedListener;

    public static CustomBottomSheet newInstance(AppCompatActivity activity) {
        return new CustomBottomSheet(activity);
    }

    public static CustomBottomSheet newInstance(AppCompatActivity activity, View view) {
        return new CustomBottomSheet(activity, view);
    }

    public static CustomBottomSheet newInstance(AppCompatActivity activity,
                                                ArrayList<BottomSheetLayoutModel> layoutModels) {
        return new CustomBottomSheet(activity, layoutModels);
    }

    public static CustomBottomSheet newInstance(AppCompatActivity activity,
                                                FragmentStatePagerAdapter pagerAdapter) {
        return new CustomBottomSheet(activity, pagerAdapter);
    }

    public CustomBottomSheet(AppCompatActivity activity) {
        this.activity = activity;
        this.view = null;
        this.layoutModels = null;
        this.pagerAdapter = null;
    }

    public CustomBottomSheet(AppCompatActivity activity, View view) {
        this.activity = activity;
        this.view = view;
        this.layoutModels = null;
        this.pagerAdapter = null;
    }

    public CustomBottomSheet(AppCompatActivity activity, ArrayList<BottomSheetLayoutModel> layoutModels) {
        this.activity = activity;
        this.layoutModels = layoutModels;
        this.pagerAdapter = null;
        this.view = null;
    }

    public CustomBottomSheet(AppCompatActivity activity, FragmentStatePagerAdapter pagerAdapter) {
        this.activity = activity;
        this.pagerAdapter = pagerAdapter;
        this.layoutModels = null;
        this.view = null;
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

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        Objects.requireNonNull(dialog).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        LinearLayout layout = view.findViewById(R.id.sheet_layout);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        TextView tvTitle = view.findViewById(R.id.title);

        if (title != null) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }

        if (this.onViewPagerMountedListener != null)
            this.onViewPagerMountedListener.onMounted(viewPager);

        if (this.view != null) {

            layout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params);
            layout.addView(this.view);

        } else if (this.layoutModels != null) {

            layout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);

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
        } else if (pagerAdapter != null) {

            layout.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setAdapter(pagerAdapter);
        }
        //dialog cancel when touches outside (Optional)
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);
    }

    public void setOnViewPagerMountedListener(OnViewPagerMountedListener onViewPagerMountedListener) {
        this.onViewPagerMountedListener = onViewPagerMountedListener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void show() {
        this.show(this.activity.getSupportFragmentManager(), this.getTag());
    }

    private LinearLayout getView(BottomSheetLayoutModel layoutModel) {

        LinearLayout layout = new LinearLayout(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(Utility.getDip(activity, 20));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);

        int padding = Utility.getDip(activity, 20);
        int paddingSides = Utility.getDip(activity, 10);
        layout.setPadding(paddingSides, padding, paddingSides, padding);
        layout.addView(getTextView(layoutModel));

        if (layoutModel.getOnClickListener() != null) layout.setOnClickListener(
                v -> layoutModel.getOnClickListener().onClick(this, v));

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
