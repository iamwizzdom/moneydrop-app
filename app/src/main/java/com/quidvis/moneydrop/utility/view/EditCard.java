package com.quidvis.moneydrop.utility.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.utility.CardPattern;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import java.util.Objects;
import java.util.regex.Pattern;

public class EditCard extends androidx.appcompat.widget.AppCompatEditText {

    private String type = "UNKNOWN";

    public EditCard(@NonNull Context context) {
        super(context);
        addListeners();
    }

    public EditCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addListeners();
    }

    public EditCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addListeners();
    }

    private void addListeners() {
        // Adding the TextWatcher
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int position, int before, int action) {
//                if (action == 1) {
//                    if (type.equals("UNKNOWN") || type.equals("Visa") || type.equals("Discover") || type.equals("JCB")) {
//                        if (position == 3 || position == 8 || position == 13) {
//                            if (!s.toString().endsWith("-")) {
//                                append("-");
//                            }
//                        }
//                    } else if (type.equals("American_Express") || type.equals("Diners_Club")) {
//                        if (position == 3 || position == 10) {
//                            if (!s.toString().endsWith("-")) {
//                                append("-");
//                            }
//                        }
//                    }
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String initial = s.toString();
                // remove all non-digits characters
                String processed = initial.replaceAll("\\D", "");
                // insert a space after all groups of 4 digits that are followed by another digit
                processed = initial.replaceAll("(\\d{4})(?=\\d)", "$1 ");
                // to avoid stackoverflow errors, check that the processed is different from what's already
                //  there before setting
                if (!initial.contentEquals(processed) && processed.length() > 0) {
                    // set the value
                    s.replace(0, initial.length(), processed);
                }
                changeIcon();
            }
        });
        // The input filters
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; ++i) {
                if (!Pattern.compile("[0-9\\s]*").matcher(String.valueOf(source)).matches()) {
                    return "";
                }
            }
            return null;
        };

        // Changing the icon when it's empty
        changeIcon();
        setCompoundDrawablePadding(Utility.getDip((Activity) getContext(), 20));
        // Setting the filters
        setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(23)});
    }

    private void changeIcon() {

        String s = getCardNumber(), st2 = "0", st4 = "0";
        int size = s.length();

        if (size >= 2) st2 = s.substring(0, 2);
        if (size >= 4) st4 = s.substring(0, 4);

        if (s.startsWith("4") && (s.matches(CardPattern.VISA) || s.matches(CardPattern.VISA_MASTER))) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visa_card, 0, 0,0);
            type = "Visa";
        } else if (s.matches(CardPattern.VERVE)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verve, 0, 0,0);
            type = "Verve";
        } else if (s.matches(CardPattern.MAESTROCARD)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_maestro, 0, 0,0);
            type = "Maestro";
        } else if (Validator.isNumberBetween(Integer.parseInt(st2), 51, 55) ||
                Validator.isNumberBetween(Integer.parseInt(st4), 2221, 2720) ||
                s.matches(CardPattern.MASTERCARD_SHORTER) || s.matches(CardPattern.MASTERCARD_SHORT) ||
                s.matches(CardPattern.MASTERCARD)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_master_card, 0, 0,0);
            type = "MasterCard";
        } else if (s.matches(CardPattern.AMERICAN_EXPRESS)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_american_express_card, 0, 0,0);
            type = "American Express";
        } else if (s.matches(CardPattern.DISCOVER_SHORT) || s.matches(CardPattern.DISCOVER)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_discover_card, 0, 0,0);
            type = "Discover";
        } else if (s.matches(CardPattern.JCB_SHORT) || s.matches(CardPattern.JCB)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_jcb_card,0, 0, 0);
            type = "JCB";
        } else if (s.matches(CardPattern.DINERS_CLUB_SHORT) || s.matches(CardPattern.DINERS_CLUB)) {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diners_club_card, 0, 0,0);
            type = "Diners Club";
        } else {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_credit_card,0, 0, 0);
            type = "Unknown";
        }
    }

    public boolean isValid() {
        if (getCardNumber().matches(CardPattern.VISA_VALID)) return true;
        else if (getCardNumber().matches(CardPattern.VERVE_VALID)) return true;
        else if (getCardNumber().matches(CardPattern.MAESTROCARD_VALID)) return true;
        else if (getCardNumber().matches(CardPattern.MASTERCARD_VALID)) return true;
        else if (getCardNumber().matches(CardPattern.AMERICAN_EXPRESS_VALID)) return true;
        else if (getCardNumber().matches(CardPattern.DISCOVER_VALID)) return true;
        else if (getCardNumber().matches(CardPattern.DINERS_CLUB_VALID)) return true;
        return getCardNumber().matches(CardPattern.JCB_VALID);
    }

    public String getCardNumber() {
        return Objects.requireNonNull(getText()).toString().replaceAll("[^0-9]", "").trim();
    }

    public String getCardType(){
        return type;
    }
}
