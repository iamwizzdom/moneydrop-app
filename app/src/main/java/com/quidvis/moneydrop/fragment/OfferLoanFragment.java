package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.cottacush.android.currencyedittext.CurrencyEditText;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.utility.Utility;

import java.text.NumberFormat;

public class OfferLoanFragment extends Fragment {

    private Activity activity;
    private TextView tvInterestRate;
    private CurrencyEditText etAmount;
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
    private int interestRate;
    private final int incrementer = 500;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_offer_loan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = requireActivity();
        format.setMaximumFractionDigits(0);

        LinearLayout amountList = view.findViewById(R.id.amount_list);
        tvInterestRate = view.findViewById(R.id.tvInterestRate);
        etAmount = view.findViewById(R.id.amount);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        ImageView ivIncrement = view.findViewById(R.id.ivIncrement),
                ivDecrement = view.findViewById(R.id.ivDecrement);

        ivIncrement.setOnClickListener(v -> {
            double amount = etAmount.getNumericValue();
            etAmount.setText(String.valueOf((int) (amount + incrementer)));
        });

        ivDecrement.setOnClickListener(v -> {
            double amount = etAmount.getNumericValue();
            if (amount > incrementer) amount = (amount - incrementer);
            else amount = 0;
            etAmount.setText(String.valueOf((int) amount));
        });

        setInterestRate(seekBar.getProgress());

        ((MainActivity) activity).setCustomTitle("Offer Loan");
        ((MainActivity) activity).setCustomSubtitle("How much would you like to offer?");

        int[] amounts = new int[]{10000, 25000, 50000, 100000, 200000, 300000, 400000,
                500000, 600000, 700000, 800000, 900000, 1000000};

        for (int i = 0; i < amounts.length; i++) {
            TextView tv = new TextView(activity);

            tv.setTextAppearance(activity, R.style.text_view_style);
            tv.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.layout_background_rounded, null));
            tv.setBackgroundTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorGrayExtraLight)));
            tv.setTextColor(activity.getResources().getColor(R.color.colorAccent));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            if (i > 0) params.leftMargin = Utility.getDip(activity, 10);
            tv.setLayoutParams(params);

            int padding = Utility.getDip(activity, 20);
            tv.setPadding(padding, padding, padding, padding);
            Typeface typeface = ResourcesCompat.getFont(activity, R.font.campton_medium);
            tv.setTypeface(typeface);

            tv.setTextSize(16);
            tv.setText(format.format(amounts[i]));
            tv.setTag(amounts[i]);
            tv.setOnClickListener(v -> etAmount.setText(String.valueOf((int) v.getTag())));
            amountList.addView(tv);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                setInterestRate(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setInterestRate(int interestRate) {
        this.interestRate = interestRate;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(interestRate + "% in return");
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.3f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvInterestRate.setText(spannableStringBuilder);
    }
}