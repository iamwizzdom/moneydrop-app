package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.quidvis.moneydrop.utility.Utility.startRevealActivity;

public class ReviewUserActivity extends CustomCompatActivity {

    private RatingBar ratingBar;
    private EditText review;
    private LoanApplication loanApplication;
    private User loanRecipient, user;
    private CircleImageView profilePic;

    public static final String LOAN_APPLICATION_OBJECT_KEY = "applicationObject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_user);

        Intent intent = getIntent();

        if (intent == null) {
            Utility.toastMessage(this, "Invalid loan application data");
            finish();
            return;
        }

        String applicationObject = intent.getStringExtra(LOAN_APPLICATION_OBJECT_KEY);

        if (applicationObject == null) {
            Utility.toastMessage(this, "Invalid loan application data");
            finish();
            return;
        }

        try {
            loanApplication = new LoanApplication(this, new JSONObject(applicationObject));
            Bundle startDestinationBundle = new Bundle();
            startDestinationBundle.putString(LOAN_APPLICATION_OBJECT_KEY, loanApplication.getApplicationObject().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid loan application data");
            finish();
            return;
        }

        Loan loan = loanApplication.getLoan();

        this.loanRecipient = loan.isLoanOffer() ? loanApplication.getApplicant() : loan.getUser();

        if (this.loanRecipient.isMe()) {
            Utility.toastMessage(this, "You can't review yourself");
            finish();
            return;
        }

        DbHelper dbHelper = new DbHelper(this);
        user = dbHelper.getUser();

        profilePic = findViewById(R.id.profile_pic);
        ratingBar = findViewById(R.id.ratingBar);
        review = findViewById(R.id.review);

        profilePic.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(ImagePreviewActivity.IMAGE_URL, this.loanRecipient.getPictureUrl());
            startRevealActivity(this, v, ImagePreviewActivity.class, bundle);
        });

        setUser();
    }

    public void setUser() {
        setLoanRecipient(this.loanRecipient);
    }

    public void setLoanRecipient(User loanRecipient) {

        TextView tvName = findViewById(R.id.account_name);
        TextView tvEmail = findViewById(R.id.account_email);

        Glide.with(ReviewUserActivity.this)
                .load(loanRecipient.getPictureUrl())
                .placeholder(loanRecipient.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(loanRecipient.getDefaultPicture())
                .into(profilePic);

        tvName.setText(String.format("%s %s", loanRecipient.getFirstname(), loanRecipient.getLastname()));
        tvEmail.setText(loanRecipient.getEmail());
        ratingBar.setRating(Double.valueOf(loanRecipient.getRating()).floatValue());
    }

    public void viewReviews(View view) {
        Intent intent = new Intent(this, UserReviewsActivity.class);
        intent.putExtra(UserReviewsActivity.USER_OBJECT_KEY, this.loanRecipient.getUserObject().toString());
        startActivity(intent);
    }

    public void sendReview(View view) {

        CircularProgressButton reviewBtn = (CircularProgressButton) view;

        HttpRequest httpRequest = new HttpRequest(this, String.format(URLContract.LOAN_APPLICANT_REVIEW_URL, loanApplication.getReference()),
                Request.Method.POST, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("review", review.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", user.getToken());
                params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                reviewBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                reviewBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) review.setText("");

                    Utility.toastMessage(ReviewUserActivity.this, object.getString("message"), true);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ReviewUserActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(ReviewUserActivity.this);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("errors") && object.getJSONObject("errors").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("errors"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", Dialog::dismiss);

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ReviewUserActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}
