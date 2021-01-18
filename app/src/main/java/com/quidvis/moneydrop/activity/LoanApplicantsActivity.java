package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.adapter.LoanApplicantAdapter;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.MainFragment;
import com.quidvis.moneydrop.fragment.WalletFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.model.Transaction;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import br.com.simplepass.loadingbutton.customViews.OnAnimationEndListener;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.quidvis.moneydrop.utility.Utility.getTheme;

public class LoanApplicantsActivity extends AppCompatActivity {

    public static final String LOAN_KEY = "loanObject";
    public final static String STATE_KEY = LoanApplicantsActivity.class.getName();
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private DbHelper dbHelper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_applicants);

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

        format.setMaximumFractionDigits(0);

        LinearLayout container = findViewById(R.id.container);
        CircleImageView mvPic = findViewById(R.id.profile_pic);
        TextView tvType = findViewById(R.id.loan_type);
        TextView tvDate = findViewById(R.id.loan_date);
        TextView tvAmount = findViewById(R.id.loan_amount);
        TextView tvStatus = findViewById(R.id.loan_status);

        tvType.setText(String.format("Loan %s", loan.getType()));
        tvDate.setText(loan.getDate());
        tvAmount.setText(format.format(loan.getAmount()));
        tvStatus.setText(Utility.ucFirst(loan.getStatus()));
        container.setBackgroundResource(R.drawable.layout_background_rounded);

        ArrayMap<String, Integer> theme = Utility.getTheme(loan.getStatus());

        User user = loan.getUser();
        Glide.with(this)
                .load(user.getPictureUrl())
                .placeholder(user.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(user.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvPic);

        tvAmount.setTextColor(this.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

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

        dbHelper = new DbHelper(this);

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

    public void grantLoan(LoanApplication application, ProgressButton btn) {

        CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(LoanApplicantsActivity.this);
        alertDialog.setIcon(application.getLoan().getType().equals("offer") ? R.drawable.ic_give_money : R.drawable.ic_receive_money);
        alertDialog.setMessage(String.format("Are you sure you want to grant %s this loan?", application.getApplicant().getFirstname()));
        alertDialog.setNegativeButton("No, cancel");
        alertDialog.setPositiveButton("Yes, proceed", v -> sendGrantLoanRequest(application, btn));
        alertDialog.display();
    }

    public void sendGrantLoanRequest(LoanApplication application, ProgressButton btn) {

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
                btn.startProgress();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                btn.stopProgress();
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
                    }

                    Utility.toastMessage(LoanApplicantsActivity.this, object.getString("message"), true);

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
                    if (object.has("error") && object.getJSONObject("error").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("error"));
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
                data.getJSONArray("transactions").put(applicationObject);

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
                nextPage != null ? (URLContract.BASE_URL + nextPage) : String.format(URLContract.LOAN_APPLICANTS_URL, loan.getReference()),
                Request.Method.GET, new HttpRequestParams() {

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

    public void onBackPressed(View view) {
        onBackPressed();
    }
}