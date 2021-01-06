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

import com.quidvis.moneydrop.R;

import java.lang.reflect.Field;
import java.util.Objects;

public class DialogSpinner extends androidx.appcompat.widget.AppCompatTextView {

    private String[] entries;
    private boolean isSelected = false;
    private String spinnerPlaceholder, dialogTitle, dialogMessage;
    private int spinnerIcon, selectedItemPosition = 0;
    private final View.OnClickListener onClickListener = v -> {

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
        dialog.show();

        doneBtn.setOnClickListener(vw -> {
            selectedItemPosition = picker.getValue();
            selected();
            dialog.dismiss();
        });

    };

    public DialogSpinner(@NonNull Context context) {
        super(context);
    }

    public DialogSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialogSpinner, 0, 0);

        setDialogTitle(a.getString(R.styleable.DialogSpinner_dialogTitle));
        setDialogMessage(a.getString(R.styleable.DialogSpinner_dialogMessage));
        setSpinnerPlaceholder(a.getString(R.styleable.DialogSpinner_spinnerPlaceholder));
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

        setCompoundDrawablesWithIntrinsicBounds(0, 0, getSpinnerIcon(),0);
        setOnClickListener(null);
    }

    public DialogSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialogSpinner, 0, 0);

        setDialogTitle(a.getString(R.styleable.DialogSpinner_dialogTitle));
        setDialogMessage(a.getString(R.styleable.DialogSpinner_dialogMessage));
        setSpinnerPlaceholder(a.getString(R.styleable.DialogSpinner_spinnerPlaceholder));
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

        setCompoundDrawablesWithIntrinsicBounds(0, 0, getSpinnerIcon(),0);
        setOnClickListener(null);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(onClickListener);
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

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    private void selected() {
        if (entries != null && entries.length > 0) {
            setText(entries[selectedItemPosition]);
            isSelected = true;
        }
    }

    private void changeDividerColor(NumberPicker picker) {
        int color = getContext().getResources().getColor(R.color.colorWhiteDark);
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
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
}
