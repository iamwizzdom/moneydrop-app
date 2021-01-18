package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.LoanOffersFragment;
import com.quidvis.moneydrop.fragment.LoanRequestsFragment;
import com.quidvis.moneydrop.fragment.MainFragment;
import com.quidvis.moneydrop.fragment.WalletFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.view.ProgressButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class LoanDetailsActivity extends AppCompatActivity {

    public static final String LOAN_KEY = "loanObject";
    private DbHelper dbHelper;
    private Loan loan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_details);

        Intent intent = getIntent();

        String loanString = intent.getStringExtra(LOAN_KEY);

        if (loanString == null) {
            Utility.toastMessage(this, "No loan passed");
            finish();
            return;
        }

        try {
            JSONObject loanObject = new JSONObject(loanString);
            loan = new Loan(this, loanObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid loan passed");
            finish();
            return;
        }

        dbHelper = new DbHelper(this);

        NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
        format.setMaximumFractionDigits(2);

        ImageView ivUserPic = findViewById(R.id.user_pic);
        TextView tvType = findViewById(R.id.loan_type);
        TextView tvFundRaiser = findViewById(R.id.fund_raiser);
        TextView tvAmount = findViewById(R.id.loan_amount);
        TextView tvReference = findViewById(R.id.loan_reference);
        TextView tvTenure = findViewById(R.id.loan_tenure);
        TextView tvInterest = findViewById(R.id.interest);
        TextView tvPurpose = findViewById(R.id.loan_purpose);
        TextView tvInterestType = findViewById(R.id.interest_type);
        TextView tvDate = findViewById(R.id.loan_date);
        TextView tvStatus = findViewById(R.id.loan_status);
        TextView tvNote = findViewById(R.id.note);

        CircularProgressButton applyBtn = findViewById(R.id.apply_btn);
        applyBtn.setText(loan.isMine() ? "View Applicants" : (loan.isHasApplied() ? "Applied" : "Apply"));
        if (!loan.isMine() && loan.isHasApplied()) {
            applyBtn.setEnabled(false);
            applyBtn.setAlpha(.7f);
            applyBtn.setOnClickListener(null);
        } else {
            applyBtn.setOnClickListener(this::viewApplicantsOrApply);
        }

        ArrayMap<String, Integer> theme = Utility.getTheme(loan.getStatus());

        User loanUser = loan.getUser();

        Glide.with(LoanDetailsActivity.this)
                .load(loanUser.getPictureUrl())
                .placeholder(loanUser.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(loanUser.getDefaultPicture())
                .into(ivUserPic);

        tvType.setText(String.format("Loan %s", loan.getType()));
        tvFundRaiser.setText(loan.isFundRaiser() ? "Yes" : "No");
        tvAmount.setText(format.format(loan.getAmount()));
        tvReference.setText(loan.getReference());
        tvTenure.setText(loan.getTenure());
        tvInterest.setText(String.format("%s percent", loan.getInterest()));
        tvPurpose.setText(loan.getType().equals("request") ? Utility.castEmpty(loan.getPurpose(), "Not specified") : "Not applicable");
        tvInterestType.setText(String.format("%s interest", loan.getInterestType()));
        tvDate.setText(loan.getDate());
        tvStatus.setText(loan.getStatus());
        tvStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));
        tvNote.setText(Utility.castEmpty(loan.getNote(), "No note"));
    }

    public void viewApplicantsOrApply(View view) {
        if (loan.isMine()) {
            Intent intent = new Intent(this, LoanApplicantsActivity.class);
            intent.putExtra(LoanApplicantsActivity.LOAN_KEY, loan.getLoanObject().toString());
            startActivity(intent);
        } else {
            applyForLoan(loan, (CircularProgressButton) view);
        }
    }

    public void applyForLoan(Loan loan, CircularProgressButton applyBtn) {

        CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(LoanDetailsActivity.this);
        alertDialog.addView(View.inflate(this, R.layout.loan_apply_confirmation_layout, null));
        alertDialog.setIcon(R.drawable.ic_remove);
        alertDialog.setMessage("Are you sure you want to apply for this loan?");
        alertDialog.setNegativeButton("No, cancel");
        alertDialog.setPositiveButton("Yes, apply", (v, d) -> {
            EditText etAmount = d.findViewById(R.id.amount);
            EditText etNote = d.findViewById(R.id.note);
            String amount = etAmount.getText().toString();
            String note = etNote.getText().toString();
            if (amount.isEmpty()) {
                Utility.toastMessage(LoanDetailsActivity.this, "Enter a valid amount");
                return;
            }
            sendLoanApplyRequest(loan, applyBtn, amount, note);
        });
        EditText etAmount = alertDialog.getDialogView().findViewById(R.id.amount);
        if (!loan.isFundRaiser()) {
            etAmount.setText(String.valueOf(loan.getAmount()));
            Utility.disableEditText(etAmount);
        }
        alertDialog.display();
    }

    public void sendLoanApplyRequest(Loan loan, CircularProgressButton applyBtn, String amount, String note) {

        HttpRequest httpRequest = new HttpRequest(this, String.format(URLContract.LOAN_APPLY_URL, loan.getReference()),
                Request.Method.POST, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
                if (!note.isEmpty()) params.put("note", note);
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
                applyBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                applyBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {
                        applyBtn.setEnabled(false);
                        applyBtn.setAlpha(.7f);
                        applyBtn.setOnClickListener(null);

                        JSONObject respObj = object.getJSONObject("response");
                        JSONObject applicationObj = respObj.getJSONObject("application");
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

                        String stateKey = (MainActivity.STATE_KEY + "-" + (loan.getType().equals("offer") ? LoanOffersFragment.class.getName() : LoanRequestsFragment.class.getName()));
                        Bundle state = Utility.getState(stateKey);
                        if (state.size() > 0) {
                            try {
                                JSONObject data = new JSONObject(Objects.requireNonNull(state.getString("data")));
                                JSONArray loans = data.getJSONArray("loans");
                                for (int i = 0; i < loans.length(); i++) {
                                    if (loan.getReference().equals(loans.getJSONObject(i).getString("uuid"))) {
                                        loans.put(i, applicationObj.getJSONObject("loan"));
                                        data.put("loans", loans);
                                        state.putString("data", data.toString());
                                        Utility.saveState(stateKey, state);
                                        break;
                                    }
                                }
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Utility.toastMessage(LoanDetailsActivity.this, object.getString("message"), true);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoanDetailsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoanDetailsActivity.this);

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
                    Utility.toastMessage(LoanDetailsActivity.this, statusCode == 503 ? error :
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