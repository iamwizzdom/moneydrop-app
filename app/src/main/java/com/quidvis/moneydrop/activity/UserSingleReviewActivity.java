package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.ArrayMap;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.Review;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSingleReviewActivity extends AppCompatActivity {

    public static final String REVIEW_OBJECT_KEY = "reviewObject";
    public final static String STATE_KEY = UserSingleReviewActivity.class.getName();
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private User user;
    private Loan loan;
    private Review review;

    private CircleImageView mvPic, mvLoanPic, mvReviewPic;
    private TextView tvUsername, tvType, tvDate, tvAmount, tvStatus, tvReviewName, tvReviewDate, tvReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_single_review);

        Intent intent = getIntent();

        String reviewString = intent.getStringExtra(REVIEW_OBJECT_KEY);

        if (reviewString == null) {
            Utility.toastMessage(this, "No review passed");
            finish();
            return;
        }

        try {
            JSONObject reviewObject = new JSONObject(reviewString);
            review = new Review(this, reviewObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid review passed");
            finish();
            return;
        }

        user = review.getUser();
        loan = review.getLoanApplication().getLoan();

        format.setMaximumFractionDigits(0);

        mvPic = findViewById(R.id.user_pic);
        tvUsername = findViewById(R.id.username);

        mvLoanPic = findViewById(R.id.profile_pic);
        tvType = findViewById(R.id.loan_type);
        tvDate = findViewById(R.id.loan_date);
        tvAmount = findViewById(R.id.loan_amount);
        tvStatus = findViewById(R.id.loan_status);

        mvReviewPic = findViewById(R.id.review_pic);
        tvReviewName = findViewById(R.id.review_name);
        tvReviewDate = findViewById(R.id.review_date);
        tvReview = findViewById(R.id.review);

        setUserView();
        setLoanView();
        setReview();
    }

    public void viewReviewUser(View view) {
        if (user.isMe()) return;
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_OBJECT_KEY, user.getUserObject().toString());
        startActivity(intent);
    }

    public void viewLoan(View view) {
        Intent intent = new Intent(this, LoanDetailsActivity.class);
        intent.putExtra(LoanDetailsActivity.LOAN_OBJECT_KEY, loan.getLoanObject().toString());
        startActivity(intent);
    }

    public void setUserView() {

        tvUsername.setText(String.format("%s %s", user.getFirstname(), user.getLastname()));

        Glide.with(this)
                .load(user.getPictureUrl())
                .placeholder(user.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(user.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvPic);
    }

    public void setLoanView() {

        tvType.setText(String.format("Loan %s", loan.getLoanType()));
        tvDate.setText(loan.getDate());
        tvAmount.setText(format.format(loan.getAmount()));
        tvStatus.setText(Utility.ucFirst(loan.getStatus()));

        User loanUser = loan.getUser();

        Glide.with(this)
                .load(loanUser.getPictureUrl())
                .placeholder(loanUser.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(loanUser.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvLoanPic);

        ArrayMap<String, Integer> theme = Utility.getTheme(loan.getStatus());

        tvAmount.setTextColor(this.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));
    }

    public void setReview() {

        User reviewer = review.getReviewer();

        Glide.with(this)
                .load(reviewer.getPictureUrl())
                .placeholder(reviewer.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(reviewer.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvReviewPic);

        tvReviewName.setText(String.format("%s %s", reviewer.getFirstname(), reviewer.getLastname()));
        mvReviewPic.setOnClickListener(v -> {
            if (reviewer.isMe()) return;
            Intent intent = new Intent(UserSingleReviewActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_OBJECT_KEY, reviewer.getUserObject().toString());
            startActivity(intent);
        }); 
        tvReviewName.setOnClickListener(v -> {
            if (reviewer.isMe()) return;
            Intent intent = new Intent(UserSingleReviewActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_OBJECT_KEY, reviewer.getUserObject().toString());
            startActivity(intent);
        });
        tvReviewDate.setText(review.getDateFormatted());
        tvReview.setText(review.getReview());
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}