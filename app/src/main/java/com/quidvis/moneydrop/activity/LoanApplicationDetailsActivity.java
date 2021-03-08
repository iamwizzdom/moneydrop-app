package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.MainFragment;
import com.quidvis.moneydrop.fragment.WalletFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class LoanApplicationDetailsActivity extends CustomCompatActivity {

    public static final String LOAN_APPLICATION_POSITION_KEY = "historyPositionKey";
    public static final String LOAN_APPLICATION_OBJECT_KEY = "applicationObject";
    public static final int LOAN_APPLICATION_REQUEST_KEY = 134;
    private NumberFormat format;
    private User user;
    private LoanApplication loanApplication;
    private Loan loan;
    private int loanApplicationKey;

    private ImageView ivProfilePic, ivUserPic;
    private TextView tvUsername, tvLoanType, tvLoanAmount, tvAmountPaid, tvAmountUnpaid, tvReference,
            tvLoanDate, tvDate, tvLoanStatus, tvDateGranted, tvDateDue, tvApplicationDate, tvApplicationStatus;
    private LinearLayout loanLayout, paymentBtnHolder;
    CircularProgressButton payBtn, cancelApplicationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_application_details);

        Intent intent = getIntent();

        loanApplicationKey = intent.getIntExtra(LOAN_APPLICATION_POSITION_KEY, 0);
        String applicationString = intent.getStringExtra(LOAN_APPLICATION_OBJECT_KEY);

        if (applicationString == null) {
            Utility.toastMessage(this, "No loan application passed");
            finish();
            return;
        }

        try {
            JSONObject applicationObject = new JSONObject(applicationString);
            loanApplication = new LoanApplication(this, applicationObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid loan application passed");
            finish();
            return;
        }


        DbHelper dbHelper = new DbHelper(this);
        user = dbHelper.getUser();

        loan = loanApplication.getLoan();

        loanLayout = findViewById(R.id.loan_layout);
        ivProfilePic = findViewById(R.id.profile_pic);
        tvLoanType = findViewById(R.id.loan_type);
        tvLoanDate = findViewById(R.id.loan_date);
        tvLoanAmount = findViewById(R.id.loan_amount);
        tvLoanStatus = findViewById(R.id.loan_status);

        ivUserPic = findViewById(R.id.user_pic);
        tvUsername = findViewById(R.id.username);
        tvDate = findViewById(R.id.date);

        format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
        format.setMaximumFractionDigits(2);

        tvReference = findViewById(R.id.application_reference);
        tvAmountPaid = findViewById(R.id.paid_amount);
        tvAmountUnpaid = findViewById(R.id.unpaid_amount);
        tvDateGranted = findViewById(R.id.date_granted);
        tvDateDue = findViewById(R.id.due_date);
        tvApplicationDate = findViewById(R.id.application_date);
        tvApplicationDate = findViewById(R.id.application_date);
        tvApplicationStatus = findViewById(R.id.application_status);

        paymentBtnHolder = findViewById(R.id.payment_btn_holder);
        payBtn = findViewById(R.id.pay_btn);
        cancelApplicationBtn = findViewById(R.id.cancel_application_btn);

        setLoanView();
        setLoanRecipientView();
        setLoanApplicationView();
    }

    public void setLoanView() {

        String type = String.format("Loan %s", loan.getLoanType());
        if (loan.isMine()) type += " (Me)";
        tvLoanType.setText(type);
        tvLoanDate.setText(loan.getDate());
        tvLoanAmount.setText(format.format(loan.getAmount()));
        tvLoanStatus.setText(Utility.ucFirst(loan.getStatus()));

        User loanUser = loan.getUser();

        Glide.with(this)
                .load(loanUser.getPictureUrl())
                .placeholder(loanUser.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(loanUser.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(ivProfilePic);

        ArrayMap<String, Integer> theme = Utility.getTheme(loan.getStatus());

        tvLoanAmount.setTextColor(this.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvLoanStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvLoanStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));
        loanLayout.setOnClickListener(v -> {
            Intent intent = new Intent(LoanApplicationDetailsActivity.this, LoanDetailsActivity.class);
            intent.putExtra(LoanDetailsActivity.LOAN_OBJECT_KEY, loan.getLoanObject().toString());
            startActivityForResult(intent, LoanApplicationDetailsActivity.LOAN_APPLICATION_REQUEST_KEY);
        });
    }

    private void setLoanRecipientView() {

        User payee = loan.isLoanOffer() ? loanApplication.getApplicant() : loan.getUser();

        Glide.with(this)
                .load(payee.getPictureUrl())
                .placeholder(payee.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(payee.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(ivUserPic);

        tvUsername.setText(String.format("%s %s", payee.getFirstname(), payee.getLastname()));

        String date = String.format("Date Applied: %s", loanApplication.getDate());
        int start = date.indexOf(":") + 1, end = date.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(date);
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDate.setText(spannableStringBuilder);
    }

    private void setLoanApplicationView() {

        tvReference.setText(loanApplication.getReference());
        tvAmountPaid.setText(format.format(loanApplication.getRepaidAmount()));
        tvAmountUnpaid.setText(format.format(loanApplication.getUnpaidAmount()));
        tvDateGranted.setText(loanApplication.getDateGranted());
        tvDateDue.setText(loanApplication.isGranted() ? loanApplication.getDueDateShort() : "Unavailable");
        tvApplicationDate.setText(loanApplication.getDate());
        tvApplicationStatus.setText(Utility.ucFirst(loanApplication.getStatus()));

        ArrayMap<String, Integer> theme = Utility.getTheme(loanApplication.getStatus());
        tvApplicationStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvApplicationStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

        if (loanApplication.getApplicant().isMe()) cancelApplicationBtn.setVisibility(loanApplication.isGranted() ? View.GONE : View.VISIBLE);

        paymentBtnHolder.setVisibility(loanApplication.isGranted() ? View.VISIBLE : View.GONE);

        if (loanApplication.isGranted()) {
            if (loan.isLoanOffer() && loan.getUser().isMe()) payBtn.setVisibility(View.GONE);
            else if (loan.isLoanRequest() && loanApplication.getApplicant().isMe()) payBtn.setVisibility(View.GONE);
            else if (loanApplication.isRepaid()) Utility.disableButton(payBtn);
        }
    }

    public void reviewRecipient(View view) {
        if ((loan.isLoanRequest() && loan.isMine()) || (loan.isLoanOffer() && !loan.isMine())) return;
        User user = loan.isLoanOffer() ? loanApplication.getApplicant() : loan.getUser();
        if (user.isMe()) return;
        Intent intent = new Intent(this, !loanApplication.isReviewed() ? ReviewUserActivity.class : ProfileActivity.class);
        if (loanApplication.isReviewed()) {
            intent.putExtra(ProfileActivity.USER_OBJECT_KEY, user.getUserObject().toString());
        } else {
            intent.putExtra(ReviewUserActivity.LOAN_APPLICATION_OBJECT_KEY, loanApplication.getApplicationObject().toString());
        }
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            Utility.toastMessage(LoanApplicationDetailsActivity.this, "Failed to capture new loan data.");
            return;
        }

        if (requestCode == LOAN_APPLICATION_REQUEST_KEY) {

            String loanString = data.getStringExtra(LoanDetailsActivity.LOAN_OBJECT_KEY);

            if (loanString == null) {
                Utility.toastMessage(this, "No loan passed");
                finish();
                return;
            }

            try {
                JSONObject loanObject = new JSONObject(loanString);
                loan = new Loan(this, loanObject);
                setLoanView();
            } catch (JSONException e) {
                e.printStackTrace();
                Utility.toastMessage(this, "Invalid loan passed");
                finish();
            }
        }
    }

    public void viewPaymentHistory(View view) {
        Intent intent = new Intent(this, LoanRepaymentTransactionsActivity.class);
        intent.putExtra(LoanRepaymentTransactionsActivity.APPLICATION_REFERENCE, loanApplication.getReference());
        startActivity(intent);
    }

    public void makePayment(View view) {

        view = getLayoutInflater().inflate(R.layout.enter_amount_bottom_sheet_layout, null);
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, view);
        LinearLayout container = view.findViewById(R.id.container);
        TextView tvTitle = view.findViewById(R.id.title);
        EditText tvAmount = view.findViewById(R.id.amount);
        TextView tvSuccess = view.findViewById(R.id.successful);
        CircularProgressButton submitBtn = view.findViewById(R.id.submit_btn);
        tvTitle.setText(String.format("Amount not greater then %s", format.format(loanApplication.getUnpaidAmount())));
        submitBtn.setText(R.string.pay);
        submitBtn.setOnClickListener(v -> {
            String amount = tvAmount.getText().toString();
            double dAmount;
            if (amount.isEmpty() || (dAmount = Double.parseDouble(amount)) <= 0) {
                Utility.toastMessage(LoanApplicationDetailsActivity.this, "Please enter a valid amount");
                return;
            }
            sendRepaymentRequest(container, tvSuccess, submitBtn, bottomSheet, String.valueOf(dAmount));
        });
        bottomSheet.show();

    }

    public void cancelApplication(View view) {
        CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(this);
        alertDialog.setIcon(R.drawable.ic_remove);
        alertDialog.setMessage("Are you sure you want to cancel your application?");
        alertDialog.setNegativeButton("No, cancel");
        alertDialog.setPositiveButton("Yes, proceed", v -> sendCancelApplicationRequest((CircularProgressButton) view));
        alertDialog.display();
    }

    public void sendRepaymentRequest(LinearLayout container, TextView tvSuccess, CircularProgressButton submitBtn, CustomBottomSheet bottomSheet, String amount) {

        HttpRequest httpRequest = new HttpRequest(this, String.format(URLContract.LOAN_REPAYMENT_URL, loanApplication.getReference()),
                Request.Method.POST, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
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
                submitBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                submitBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        JSONObject respObj = object.getJSONObject("response");
                        JSONObject applicationObj = respObj.getJSONObject("application");
                        loanApplication = new LoanApplication(LoanApplicationDetailsActivity.this, applicationObj);
                        setLoanView();
                        setLoanApplicationView();

                        double balance = respObj.getDouble("balance");
                        double availableBalance = respObj.getDouble("available_balance");

                        Bundle mainFragmentState = Utility.getState(MainFragment.STATE_KEY);
                        String mainFragmentStringData = mainFragmentState.getString("data");

                        if (mainFragmentStringData != null) {

                            JSONObject mainFragmentData = new JSONObject(mainFragmentStringData);

                            mainFragmentData.put("balance", balance);
                            mainFragmentData.put("available_balance", availableBalance);

                            mainFragmentState.putString("data", mainFragmentData.toString());
                            Utility.saveState(MainFragment.STATE_KEY, mainFragmentState);
                        }

                        Bundle walletFragmentState = Utility.getState(WalletFragment.STATE_KEY);
                        String walletFragmentStringData = walletFragmentState.getString("data");

                        if (walletFragmentStringData != null) {

                            JSONObject walletFragmentData = new JSONObject(walletFragmentStringData);

                            walletFragmentData.put("balance", balance);
                            walletFragmentData.put("available_balance", availableBalance);

                            walletFragmentState.putString("data", walletFragmentData.toString());
                            Utility.saveState(WalletFragment.STATE_KEY, walletFragmentState);
                        }

                        tvSuccess.setText(object.getString("message"));
                        container.setVisibility(View.GONE);
                        tvSuccess.setVisibility(View.VISIBLE);
                        submitBtn.setOnClickListener(v -> bottomSheet.dismiss());
                        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                            submitBtn.setText(R.string.done);
                            submitBtn.setPadding(30, 0, 30, 0);
                        }, 500);

                    } else Utility.toastMessage(LoanApplicationDetailsActivity.this, object.getString("message"), true);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoanApplicationDetailsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoanApplicationDetailsActivity.this);

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
                    Utility.toastMessage(LoanApplicationDetailsActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void sendCancelApplicationRequest(CircularProgressButton cancelBtn) {

        HttpRequest httpRequest = new HttpRequest(this, String.format(URLContract.LOAN_APPLICATION_CANCEL_URL, loan.getUuid(), loanApplication.getReference()),
                Request.Method.POST, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                return null;
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
                cancelBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                cancelBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        JSONObject respObj = object.getJSONObject("response");
                        JSONObject applicationObj = respObj.getJSONObject("application");
                        loanApplication = new LoanApplication(LoanApplicationDetailsActivity.this, applicationObj);
                        setLoanApplicationView();

                        double balance = respObj.getDouble("balance");
                        double availableBalance = respObj.getDouble("available_balance");

                        Bundle mainFragmentState = Utility.getState(MainFragment.STATE_KEY);
                        String mainFragmentStringData = mainFragmentState.getString("data");

                        if (mainFragmentStringData != null) {

                            JSONObject mainFragmentData = new JSONObject(mainFragmentStringData);

                            mainFragmentData.put("balance", balance);
                            mainFragmentData.put("available_balance", availableBalance);

                            mainFragmentState.putString("data", mainFragmentData.toString());
                            Utility.saveState(MainFragment.STATE_KEY, mainFragmentState);
                        }

                        Bundle walletFragmentState = Utility.getState(WalletFragment.STATE_KEY);
                        String walletFragmentStringData = walletFragmentState.getString("data");

                        if (walletFragmentStringData != null) {

                            JSONObject walletFragmentData = new JSONObject(walletFragmentStringData);

                            walletFragmentData.put("balance", balance);
                            walletFragmentData.put("available_balance", availableBalance);

                            walletFragmentState.putString("data", walletFragmentData.toString());
                            Utility.saveState(WalletFragment.STATE_KEY, walletFragmentState);
                        }

                        Utility.disableButton(cancelBtn);

                    }

                    Utility.toastMessage(LoanApplicationDetailsActivity.this, object.getString("message"), true);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoanApplicationDetailsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoanApplicationDetailsActivity.this);

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
                    Utility.toastMessage(LoanApplicationDetailsActivity.this, statusCode == 503 ? error :
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
        intent.putExtra(LOAN_APPLICATION_POSITION_KEY, loanApplicationKey);
        intent.putExtra(LOAN_APPLICATION_OBJECT_KEY, loanApplication.getApplicationObject().toString());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}