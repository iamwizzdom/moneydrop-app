package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.ArrayMap;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.Review;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSingleReviewActivity extends AppCompatActivity {

    public static final String REVIEW_POSITION_KEY = "reviewPositionKey";
    public static final String REVIEW_OBJECT_KEY = "reviewObject";
    public final static String STATE_KEY = UserSingleReviewActivity.class.getName();
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private int reviewKey;
    private User user;
    private Loan loan;
    private Review review;
    private DbHelper dbHelper;

    private ProgressBar reviewProgress;
    private CircleImageView mvPic, mvLoanPic, mvReviewPic;
    private TextView tvUsername, tvType, tvDate, tvAmount, tvStatus, tvReviewName, tvReviewDate, tvReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_single_review);

        Intent intent = getIntent();

        reviewKey = intent.getIntExtra(REVIEW_POSITION_KEY, 0);
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

        dbHelper = new DbHelper(this);

        user = review.getUser();
        loan = review.getLoanApplication().getLoan();

        format.setMaximumFractionDigits(0);

        reviewProgress = findViewById(R.id.reviewProgress);
        mvPic = findViewById(R.id.user_pic);
        tvUsername = findViewById(R.id.username);

        mvLoanPic = findViewById(R.id.profile_pic);
        tvType = findViewById(R.id.loan_type);
        tvDate = findViewById(R.id.loan_date);
        tvAmount = findViewById(R.id.loan_amount);
        tvStatus = findViewById(R.id.loan_status);

        ImageView ivOptionBtn = findViewById(R.id.option);

        mvReviewPic = findViewById(R.id.review_pic);
        tvReviewName = findViewById(R.id.review_name);
        tvReviewDate = findViewById(R.id.review_date);
        tvReview = findViewById(R.id.review);

        setUserView();
        setLoanView();
        setReview();

        if (review.getReviewer().isMe()) {

            ArrayList<BottomSheetLayoutModel> layoutModels = new ArrayList<>();

            BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
            sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_edit, null));
            sheetLayoutModel.setText(getResources().getString(R.string.edit_review));
            sheetLayoutModel.setOnClickListener((sheet, v) -> {
                CustomBottomAlertDialog dialog = new CustomBottomAlertDialog(UserSingleReviewActivity.this);
                dialog.addView(View.inflate(this, R.layout.review_update_layout, null));
                dialog.setIcon(R.drawable.ic_update);
                dialog.setMessage("Edit Review");
                dialog.setNegativeButton("Cancel");
                dialog.setPositiveButton("Update", (v1, d) -> {
                    EditText etReview = d.findViewById(R.id.review);
                    String review = etReview.getText().toString();
                    if (review.isEmpty()) {
                        Utility.toastMessage(UserSingleReviewActivity.this, "Enter a valid review");
                        return;
                    }
                    update(review);
                });
                sheet.dismiss();
                EditText etReview = dialog.getDialogView().findViewById(R.id.review);
                if (etReview != null) etReview.setText(review.getReview());
                dialog.display();
            });

            layoutModels.add(sheetLayoutModel);

            sheetLayoutModel = new BottomSheetLayoutModel();
            sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_delete, null));
            sheetLayoutModel.setText(getResources().getString(R.string.delete));
            sheetLayoutModel.setOnClickListener((sheet, v) -> {
                CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(UserSingleReviewActivity.this);
                alertDialog.setIcon(R.drawable.ic_remove);
                alertDialog.setMessage("Are you sure you want to delete this review?");
                alertDialog.setNegativeButton("No, cancel");
                alertDialog.setPositiveButton("Yes, proceed", vw -> delete());
                sheet.dismiss();
                alertDialog.display();
            });

            layoutModels.add(sheetLayoutModel);

            CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, layoutModels);

            ivOptionBtn.setOnClickListener(v -> bottomSheet.show());

        } else ivOptionBtn.setVisibility(View.INVISIBLE);
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

    private void update(String reviewTxt) {

        HttpRequest httpRequest = new HttpRequest(UserSingleReviewActivity.this,
                String.format( URLContract.EDIT_REVIEW_URL, review.getUuid()),
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("review", reviewTxt);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", String.format("Bearer %s", dbHelper.getUser().getToken()));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                Utility.fadeIn(UserSingleReviewActivity.this, reviewProgress);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                Utility.fadeOut(UserSingleReviewActivity.this, reviewProgress);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    JSONObject responseObj = object.getJSONObject("response");

                    if (object.getBoolean("status")) {
                        review = new Review(UserSingleReviewActivity.this, responseObj.getJSONObject("review"));
                        setReview();
                    }

                    Utility.toastMessage(UserSingleReviewActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(UserSingleReviewActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(UserSingleReviewActivity.this);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("error") && object.getJSONObject("error").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("error"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", Dialog::dismiss);

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(UserSingleReviewActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void delete() {

        HttpRequest httpRequest = new HttpRequest(UserSingleReviewActivity.this,
                String.format( URLContract.DELETE_REVIEW_URL, review.getUuid()),
                Request.Method.DELETE, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", String.format("Bearer %s", dbHelper.getUser().getToken()));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                Utility.fadeIn(UserSingleReviewActivity.this, reviewProgress);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                Utility.fadeOut(UserSingleReviewActivity.this, reviewProgress);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {
                        review = null;
                        onBackPressed();
                    }

                    Utility.toastMessage(UserSingleReviewActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(UserSingleReviewActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(UserSingleReviewActivity.this);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("error") && object.getJSONObject("error").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("error"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", Dialog::dismiss);

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(UserSingleReviewActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(REVIEW_POSITION_KEY, reviewKey);
        intent.putExtra(REVIEW_OBJECT_KEY, review != null ? review.getReviewObject().toString() : null);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}