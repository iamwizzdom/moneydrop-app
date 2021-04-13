package com.quidvis.moneydrop.utility.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.quidvis.moneydrop.R;

import java.lang.reflect.Field;
import java.util.Objects;

public class DialogSpinner extends androidx.appcompat.widget.AppCompatTextView {

    private String[] entries;
    private boolean isSelected = false;
    private String spinnerPlaceholder, dialogTitle, dialogMessage;
    private int originalTextColor, spinnerPlaceholderColor,
            spinnerIcon, selectedItemPosition = 0;
    private OnSelectedListener onSelectedListener;
    private final OnClickListener onClickListener = v -> {

        AlertDialog dialog = new AlertDialog(getContext(), R.layout.spinner_layout);

        NumberPicker picker = dialog.findViewById(R.id.picker);
        TextView title = dialog.findViewById(R.id.dialog_title);
        TextView message = dialog.findViewById(R.id.dialog_message);
        Button doneBtn = dialog.findViewById(R.id.done_btn);

        if (this.dialogTitle != null) title.setText(this.dialogTitle);
        else title.setVisibility(GONE);

        if (this.dialogMessage != null) message.setText(this.dialogMessage);
        else message.setVisibility(GONE);

        picker.setMinValue(0);
        if (entries != null && entries.length > 0) {
            picker.setMaxValue(entries.length - 1);
            picker.setDisplayedValues(entries);
        }

        changeDividerColor(picker);
        setNumberPickerTextColor(picker);
        picker.setValue(selectedItemPosition);

        doneBtn.setOnClickListener(vw -> {
            selectedItemPosition = picker.getValue();
            selected();
            if (isSelected && onSelectedListener != null) {
                onSelectedListener.onSelected(v, getSelectedItem(), selectedItemPosition);
            }
            dialog.dismiss();
        });

        dialog.show();
    };

    public DialogSpinner(@NonNull Context context) {
        super(context);
    }

    public DialogSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialogSpinner, 0, 0);

        originalTextColor = getCurrentTextColor();
        setDialogTitle(a.getString(R.styleable.DialogSpinner_dialogTitle));
        setDialogMessage(a.getString(R.styleable.DialogSpinner_dialogMessage));
        setSpinnerPlaceholder(a.getString(R.styleable.DialogSpinner_spinnerPlaceholder));
        setSpinnerPlaceholderColor(a.getResourceId(R.styleable.DialogSpinner_spinnerPlaceholderColor, R.color.black));
        setSpinnerIcon(a.getResourceId(R.styleable.DialogSpinner_spinnerIcon, R.drawable.ic_down_arrow));

        CharSequence[] entry = a.getTextArray(R.styleable.DialogSpinner_entries);
        int size = entry != null ? entry.length : 0;
        String[] e = new String[size];
        for (int i = 0; i < size; i++) {
            if (entry[i] == null) continue;
            e[i] = entry[i].toString();
        }
        if (e.length > 0) setEntries(e);

        if (spinnerPlaceholder == null && entries != null && entries.length > 0) {
            setText(entries[selectedItemPosition]);
        } else setText(spinnerPlaceholder);

        setTextColor(ResourcesCompat.getColor(getResources(), spinnerPlaceholderColor, null));
        setCompoundDrawablesWithIntrinsicBounds(0, 0, getSpinnerIcon(),0);
        setOnClickListener(null);
    }

    public DialogSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialogSpinner, 0, 0);

        originalTextColor = getCurrentTextColor();

        setDialogTitle(a.getString(R.styleable.DialogSpinner_dialogTitle));
        setDialogMessage(a.getString(R.styleable.DialogSpinner_dialogMessage));
        setSpinnerPlaceholder(a.getString(R.styleable.DialogSpinner_spinnerPlaceholder));
        setSpinnerIcon(a.getResourceId(R.styleable.DialogSpinner_spinnerIcon, R.drawable.ic_down_arrow));
        setSpinnerPlaceholderColor(a.getResourceId(R.styleable.DialogSpinner_spinnerPlaceholderColor, R.color.black));

        CharSequence[] entry = a.getTextArray(R.styleable.DialogSpinner_entries);
        int size = entry != null ? entry.length : 0;
        String[] e = new String[size];
        for (int i = 0; i < size; i++) {
            if (entry[i] == null) continue;
            e[i] = entry[i].toString();
        }
        if (e.length > 0) setEntries(e);

        if (spinnerPlaceholder == null && entries != null && entries.length > 0) {
            setText(entries[selectedItemPosition]);
        } else setText(spinnerPlaceholder);

        setTextColor(ResourcesCompat.getColor(getResources(), spinnerPlaceholderColor, null));
        setCompoundDrawablesWithIntrinsicBounds(0, 0, getSpinnerIcon(),0);
        setOnClickListener(null);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(onClickListener);
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
    }

    public int getSpinnerIcon() {
        return spinnerIcon;
    }

    public void setSpinnerPlaceholder(String spinnerPlaceholder) {
        this.spinnerPlaceholder = spinnerPlaceholder;
    }

    public void setSpinnerPlaceholderColor(int color) {
        this.spinnerPlaceholderColor = color;
    }

    public void setSpinnerIcon(int spinnerIcon) {
        this.spinnerIcon = spinnerIcon;
    }

    public void setEntries(String[] entries) {
        this.entries = entries;
    }

    public String[] getEntries() {
        return entries;
    }

    public String getSelectedItem() {
        return isSelected ? entries[selectedItemPosition] : null;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
        selected();
    }

    public void setSelectedItem(String selectedItem) {
        int size = entries.length;
        for (int i = 0; i < size; i++) {
            String entry = entries[i];
            if (entry == null) continue;
            if (entry.equals(selectedItem)) {
                this.selectedItemPosition = i;
                selected();
                break;
            }
        }
    }

    public void performSelected() {
        selected();
        if (isSelected && onSelectedListener != null) {
            onSelectedListener.onSelected(this, getSelectedItem(), selectedItemPosition);
        }
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    private void selected() {
        if (entries != null && entries.length > selectedItemPosition) {
            setText(entries[selectedItemPosition]);
            isSelected = true;
            setTextColor(originalTextColor);
        }
    }

    private void changeDividerColor(NumberPicker picker) {
        int color = getContext().getResources().getColor(R.color.colorWhiteDark);
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void setNumberPickerTextColor(NumberPicker numberPicker) {
        int color = getContext().getResources().getColor(R.color.colorBlackLight);

        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) Objects.requireNonNull(selectorWheelPaintField.get(numberPicker))).setColor(color);
                    ((EditText) child).setTextColor(color);
                    child.setEnabled(false);
                    child.setFocusable(false);
                    numberPicker.invalidate();
                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class AlertDialog extends Dialog {

        public AlertDialog(@NonNull Context context, int resId) {
            super(context);
            setContentView(resId);
            Window window = getWindow();
            assert window != null;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.getAttributes().windowAnimations = R.style.DialogAnimationPop;
        }
    }

    public interface OnSelectedListener {
        void onSelected(View v, String item, int position);
    }
}
