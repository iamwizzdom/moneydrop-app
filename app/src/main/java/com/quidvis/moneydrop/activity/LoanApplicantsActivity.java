package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.adapter.LoanApplicantAdapter;
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
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoanApplicantsActivity extends CustomCompatActivity {

    public static final String LOAN_OBJECT = "loanObject";
    public final static String STATE_KEY = LoanApplicantsActivity.class.getName();
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private User user;
    private Loan loan;
    private LoanApplicantAdapter loanApplicantAdapter;
    private final ArrayList<LoanApplication> loanApplications = new ArrayList<>();
    private Bundle state;
    private JSONObject data;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvItemCount, tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    private CircleImageView mvPic;
    private TextView tvType, tvDate, tvAmount, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_applicants);

        Intent intent = getIntent();

        String loanString = intent.getStringExtra(LOAN_OBJECT);

        if (loanString == null) {
            Utility.toastMessage(this, "No loan passed");
            finish();
            return;
        }

        try {
            JSONObject loanObject = new JSONObject(loanString);
            Log.e("loanObject", loanObject.toString());
            loan = new Loan(this, loanObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid loan passed");
            finish();
            return;
        }

        format.setMaximumFractionDigits(0);

        mvPic = findViewById(R.id.profile_pic);
        tvType = findViewById(R.id.loan_type);
        tvDate = findViewById(R.id.loan_date);
        tvAmount = findViewById(R.id.loan_amount);
        tvStatus = findViewById(R.id.loan_status);

        setUpLoanView();

        tvItemCount = findViewById(R.id.item_count);
        tvItemCount.setText(R.string.no_record);
        tvNoContent = findViewById(R.id.no_content);
        shimmerFrameLayout = findViewById(R.id.shimmer_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getLoanApplications(null);
        });

        recyclerView = findViewById(R.id.applicant_list);

        DbHelper dbHelper = new DbHelper(this);
        user = dbHelper.getUser();

        loanApplicantAdapter = new LoanApplicantAdapter(recyclerView, this, loanApplications);
        recyclerView.setAdapter(loanApplicantAdapter);
        loanApplicantAdapter.setOnLoadMoreListener(() -> getLoanApplications(state.getString("nextPage")));

        state = getState();

        if (state != null && state.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(state.getString("data")));
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        getLoanApplications(null);
    }

    public void viewApplicant(User applicant) {
        if (applicant.isMe()) return;
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_OBJECT_KEY, applicant.getUserObject().toString());
        startActivity(intent);
    }

    public void setUpLoanView() {

//        LinearLayout container = findViewById(R.id.container);

        tvType.setText(String.format("Loan %s", loan.getLoanType()));
        tvDate.setText(loan.getDate());
        tvAmount.setText(format.format(loan.getAmount()));
        tvStatus.setText(Utility.ucFirst(loan.getStatus()));
//        container.setBackgroundResource(R.drawable.layout_background_rounded);
//        container.setElevation(2f);

        User loanUser = loan.getUser();

        Glide.with(this)
                .load(loanUser.getPictureUrl())
                .placeholder(loanUser.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(loanUser.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvPic);

        ArrayMap<String, Integer> theme = Utility.getTheme(loan.getStatus());

        tvAmount.setTextColor(this.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));
    }

    public void grantLoan(LoanApplication application, CircularProgressButton btn) {

        CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(LoanApplicantsActivity.this);
        alertDialog.setIcon(application.getLoan().isLoanOffer() ? R.drawable.ic_give_money : R.drawable.ic_receive_money);
        String confirmation = "Are you sure you want to give %s this loan?";
        if (application.getLoan().getLoanType().equals("request")) {
            confirmation = "Are you sure you want to collect this loan from %s?";
        }
        alertDialog.setMessage(String.format(confirmation, application.getApplicant().getFirstname()));
        alertDialog.setNegativeButton("No, cancel");
        alertDialog.setPositiveButton("Yes, proceed", v -> sendGrantLoanRequest(application, btn));
        alertDialog.display();
    }

    public void sendGrantLoanRequest(LoanApplication application, CircularProgressButton btn) {

        HttpRequest httpRequest = new HttpRequest(this, String.format(URLContract.LOAN_APPLICATION_GRANT_URL,
                application.getLoanID(), application.getReference()),
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
                btn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                btn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
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

                    LoanApplication newApplication = new LoanApplication(LoanApplicantsActivity.this, applicationObj);

                    if (newApplication.isGranted()) {

                        int size = loanApplications.size(); boolean hasApplication = false;

                        for (int i = 0; i < size; i++) {
                            LoanApplication application = loanApplications.get(i);
                            if (application.getReference().equals(newApplication.getReference())) {
                                loanApplications.set(i, newApplication);
                                hasApplication = true;
                                break;
                            }
                        }

                        if (hasApplication) {
                            for (int i = 0; i < size; i++) {
                                LoanApplication application = loanApplications.get(i);
                                application.setHasGranted(true);
                                loanApplications.set(i, application);
                            }
                            loanApplicantAdapter.notifyDataSetChanged();
                        }

                        loan = new Loan(LoanApplicantsActivity.this, applicationObj.getJSONObject("loan"));
                        setUpLoanView();

                        Intent intent = new Intent(LoanApplicantsActivity.this, LoanApprovedActivity.class);
                        intent.putExtra(LoanApprovedActivity.LOAN_APPLICATION_KEY, applicationObj.toString());
                        intent.putExtra(LoanApprovedActivity.LOAN_APPROVAL_MESSAGE_KEY, object.getString("message"));
                        startActivity(intent);

                    } else {
                        Utility.toastMessage(LoanApplicantsActivity.this, object.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoanApplicantsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoanApplicantsActivity.this);

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
                    Utility.toastMessage(LoanApplicantsActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void setLoading(boolean status) {
        setLoading(status, false);
    }

    public void setLoading(boolean status, boolean hasContent) {
        if (status) {

            tvNoContent.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();

        } else {

            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            tvNoContent.setVisibility(hasContent ? View.GONE : View.VISIBLE);
            recyclerView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        }
    }

    private void setApplications(JSONArray applications, boolean addUp) {

        int size = applications.length();

        for (int i = 0; i < size; i++) {
            try {

                JSONObject applicationObject = applications.getJSONObject(i);
                LoanApplication loanApplication = new LoanApplication(this, applicationObject);
                this.loanApplications.add(loanApplication);
                if (!addUp) continue;
                data.getJSONArray("applications").put(applicationObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = this.loanApplications.size();
        tvItemCount.setText((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : getResources().getText(R.string.no_record));
        loanApplicantAdapter.notifyDataSetChanged();
    }

    private void getLoanApplications(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            loanApplicantAdapter.setLoading(false);
            loanApplicantAdapter.setPermitLoadMore(false);
            loanApplications.add(null);
            loanApplicantAdapter.notifyItemInserted(loanApplications.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this,
                nextPage != null ? nextPage : String.format(URLContract.LOAN_APPLICANTS_URL, loan.getUuid()),
                Request.Method.GET, new HttpRequestParams() {

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

                loanApplicantAdapter.setLoading(true);
                loanApplicantAdapter.setPermitLoadMore(false);
                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                loanApplicantAdapter.setLoading(false);
                loanApplicantAdapter.setPermitLoadMore(true);
                if (onError && data == null) setLoading(false);

                if (isRefreshing()) setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setApplications(object.getJSONArray("applications"), data != null);
                    state.putString("nextPage", object.getJSONObject("pagination").getString("nextPage"));
                    if (data == null) data = object;

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (data == null) setLoading(false);
                    Utility.toastMessage(LoanApplicantsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(LoanApplicantsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoanApplicantsActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();

    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        swipeRefreshLayout.setRefreshing(refreshing);
        if (refreshing) {
            data = null;
            state.putBundle("data", null);
            state.putString("nextPage", null);
            loanApplications.clear();
            if (!loanApplicantAdapter.isPermitLoadMore()) loanApplicantAdapter.setPermitLoadMore(true);
        }
    }

    public Bundle getState() {
        return Utility.getState(STATE_KEY);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(LoanDetailsActivity.LOAN_OBJECT_KEY, loan.getLoanObject().toString());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}