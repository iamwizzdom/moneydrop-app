package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class LoanApprovedActivity extends CustomCompatActivity {

    public static final String LOAN_APPLICATION_KEY = "loanObject";
    public static final String LOAN_APPROVAL_MESSAGE_KEY = "approvalMessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_approved);

        Intent intent = getIntent();

        String loanApplicationString = intent.getStringExtra(LOAN_APPLICATION_KEY);
        String message = intent.getStringExtra(LOAN_APPROVAL_MESSAGE_KEY);

        if (loanApplicationString == null) {
            Utility.toastMessage(this, "No loan application passed");
            finish();
            return;
        }

        LoanApplication loanApplication;
        try {
            JSONObject loanApplicationObject = new JSONObject(loanApplicationString);
            loanApplication = new LoanApplication(this, loanApplicationObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid loan application passed");
            finish();
            return;
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
        format.setMaximumFractionDigits(2);

        TextView tvAmount = findViewById(R.id.amount);
        TextView tvMessage = findViewById(R.id.message);
        TextView tvInterest = findViewById(R.id.interest);

        ImageView ivUserPic = findViewById(R.id.user_pic);
        TextView tvUsername = findViewById(R.id.username);
        TextView tvDate = findViewById(R.id.date);

        Loan loan = loanApplication.getLoan();

        tvAmount.setText(format.format(loan.getAmount()));
        if (message != null && !message.isEmpty()) tvMessage.setText(message);

        String interestInfo = String.format("%s%% increase after %s", loan.getInterest(), loan.getTenure());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(interestInfo);
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.3f), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvInterest.setText(spannableStringBuilder);

        User applicant = loan.getLoanType().equals("request") ? loan.getUser() : loanApplication.getApplicant();

        Glide.with(this)
                .load(applicant.getPicture())
                .placeholder(applicant.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(applicant.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(ivUserPic);
        tvUsername.setText(String.format("%s %s", applicant.getFirstname(), applicant.getLastname()));
        String date = String.format("Due Date: %s", loanApplication.getDueDateShort());
        int start = date.indexOf(":") + 1, end = date.length();
        spannableStringBuilder = new SpannableStringBuilder(date);
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDate.setText(spannableStringBuilder);
    }

    public void onBackPressed(View view) {
        onBackPressed();
        finish();
    }
}