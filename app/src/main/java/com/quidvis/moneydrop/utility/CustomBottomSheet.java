package com.quidvis.moneydrop.utility;

import android.content.DialogInterface;
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
    private ArrayList<BottomSheetLayoutModel> layoutModels;
    private int resource;
    private LinearLayout sheetContainerLayout, sheetViewLayout;
    private CharSequence title, message;
    private TextView tvTitle, tvMessage;
    private OnViewInflatedListener onViewInflatedListener;
    private int titleGravity, messageGravity;
    private OnDismissListener onDismissListener;

    public static CustomBottomSheet newInstance(AppCompatActivity activity) {
        return new CustomBottomSheet(activity);
    }

    public static CustomBottomSheet newInstance(AppCompatActivity activity, int resource) {
        return new CustomBottomSheet(activity, resource);
    }

    public static CustomBottomSheet newInstance(AppCompatActivity activity, ArrayList<BottomSheetLayoutModel> layoutModels) {
        return new CustomBottomSheet(activity, layoutModels);
    }

    public CustomBottomSheet(AppCompatActivity activity) {
        this.activity = activity;
        this.resource = 0;
        this.layoutModels = null;
    }

    public CustomBottomSheet(AppCompatActivity activity, int resource) {
        this.activity = activity;
        setViewResource(resource);
    }

    public CustomBottomSheet(AppCompatActivity activity, ArrayList<BottomSheetLayoutModel> layoutModels) {
        this.activity = activity;
        setLayoutModel(layoutModels);
    }

    public void setViewResource(int resource) {
        this.resource = resource;
        this.layoutModels = null;
    }

    public void addLayoutModel(BottomSheetLayoutModel layoutModel) {
        if (layoutModels == null) layoutModels = new ArrayList<>();
        layoutModels.add(layoutModel);
        this.resource = 0;
    }

    public void setLayoutModel(ArrayList<BottomSheetLayoutModel> layoutModels) {
        this.layoutModels = layoutModels;
        this.resource = 0;
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

        sheetContainerLayout = view.findViewById(R.id.sheet_container_layout);
        sheetViewLayout = view.findViewById(R.id.sheet_layout);
        tvTitle = view.findViewById(R.id.title);
        tvMessage = view.findViewById(R.id.message);

        if (title != null) {
            tvTitle.setText(title);
            tvTitle.setGravity(titleGravity);
            tvTitle.setVisibility(View.VISIBLE);
        }

        if (message != null) {
            tvMessage.setText(message);
            tvMessage.setGravity(messageGravity);
            tvMessage.setVisibility(View.VISIBLE);
        }

        if (this.resource != 0) {

            sheetViewLayout.setVisibility(View.VISIBLE);
            View view1 = activity.getLayoutInflater().inflate(this.resource, sheetViewLayout, false);
            sheetViewLayout.addView(view1);
            if (onViewInflatedListener != null) onViewInflatedListener.onInflated(view1);

        } else if (this.layoutModels != null) {

            sheetViewLayout.setVisibility(View.VISIBLE);

            int size = this.layoutModels.size();

            for (int i = 0; i < size; i++) {
                BottomSheetLayoutModel layoutModel = this.layoutModels.get(i);
                LinearLayout linearLayout = getView(layoutModel);
                if (size > 1 && i < (size - 1)) linearLayout.setBackground(
                        ResourcesCompat.getDrawable(activity.getResources(),
                                R.drawable.layout_underline, null)
                );
                sheetViewLayout.addView(linearLayout);
            }
        }
        //dialog cancel when touches outside (Optional)
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void setTitle(String title) {
        setTitle(title, Gravity.START);
    }

    public void setTitle(String title, int gravity) {
        this.title = title;
        this.titleGravity = gravity;
        if (tvTitle != null) {
            tvTitle.setText(title);
            tvTitle.setGravity(titleGravity);
            tvTitle.setVisibility(title != null ? View.VISIBLE : View.GONE);
        }
    }

    public void setMessage(CharSequence message) {
        setMessage(message, Gravity.START);
    }

    public void setMessage(CharSequence message, int gravity) {
        this.message = message;
        this.messageGravity = gravity;
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setGravity(messageGravity);
            tvMessage.setVisibility(message != null ? View.VISIBLE : View.GONE);
        }
    }

    public LinearLayout getSheetContainerLayout() {
        return sheetContainerLayout;
    }

    public LinearLayout getSheetViewLayout() {
        return sheetViewLayout;
    }

    public void setOnViewInflatedListener(OnViewInflatedListener listener) {
        onViewInflatedListener = listener;
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = Utility.getDip(activity, 10);

        tv.setTextAppearance(activity, R.style.text_view_style);
        tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.titleColorGray, null));
        Typeface typeface = ResourcesCompat.getFont(activity, R.font.campton_light);
        tv.setTypeface(typeface, Typeface.BOLD);
        tv.setLayoutParams(params);

        tv.setGravity(Gravity.START|Gravity.CENTER);
        tv.setCompoundDrawablesWithIntrinsicBounds(layoutModel.getIconLeft(), null, layoutModel.getIconRight(), null);
        tv.setCompoundDrawablePadding(Utility.getDip(activity, 20));
        tv.setText(layoutModel.getText());

        return tv;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) onDismissListener.dismissed();
    }

    public interface OnViewInflatedListener {
        void onInflated(View view);
    }

    public interface OnDismissListener {
        void dismissed();
    }

}
