package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.adapter.LoanRepaymentTransactionAdapter;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.LoanRepayment;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoanRepaymentTransactionsActivity extends CustomCompatActivity {

    public final static String STATE_KEY = LoanRepaymentTransactionsActivity.class.getName(), APPLICATION_REFERENCE = "applicationReference";
    private DbHelper dbHelper;
    private LoanRepaymentTransactionAdapter repaymentTransactionAdapter;
    private final ArrayList<LoanRepayment> repayments = new ArrayList<>();
    private Bundle state;
    private JSONObject data;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvItemCount, tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    private String applicationReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_repayment_transaction);

        Intent intent = getIntent();

        if (intent != null) applicationReference = intent.getStringExtra(APPLICATION_REFERENCE);

        tvItemCount = findViewById(R.id.item_count);
        tvItemCount.setText(R.string.no_record);
        tvNoContent = findViewById(R.id.no_content);
        shimmerFrameLayout = findViewById(R.id.shimmer_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getTransactions(null);
        });

        recyclerView = findViewById(R.id.transaction_list);

        dbHelper = new DbHelper(this);

        repaymentTransactionAdapter = new LoanRepaymentTransactionAdapter(recyclerView, this, repayments);
        recyclerView.setAdapter(repaymentTransactionAdapter);
        repaymentTransactionAdapter.setOnLoadMoreListener(() -> getTransactions(state.getString("nextPage")));

        state = getState();

        if (state != null && state.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(state.getString("data")));
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setTransaction(data.getJSONArray("repayments"), false);
            } catch (JSONException e) {
                getTransactions(null);
                e.printStackTrace();
            }

        } else {
            getTransactions(null);
        }
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

        if (isRefreshing()) setRefreshing(false);
    }

    private void setTransaction(JSONArray repayments, boolean addUp) {

        int size = repayments.length();

        if (!addUp) this.repayments.clear();

        for (int i = 0; i < size; i++) {
            try {

                JSONObject repaymentObject = repayments.getJSONObject(i);
                LoanRepayment repayment = new LoanRepayment(this, repaymentObject);
                this.repayments.add(repayment);
                if (!addUp) continue;
                data.getJSONArray("repayments").put(repaymentObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = this.repayments.size();
        tvItemCount.setText((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : getResources().getText(R.string.no_record));
        repaymentTransactionAdapter.notifyDataSetChanged();
    }

    private void getTransactions(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            repaymentTransactionAdapter.setLoading(false);
            repaymentTransactionAdapter.setPermitLoadMore(false);
            repayments.add(null);
            repaymentTransactionAdapter.notifyItemInserted(repayments.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this,
                nextPage != null ? nextPage : String.format(URLContract.LOAN_REPAYMENT_HISTORY_URL, applicationReference),
                Request.Method.GET, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", dbHelper.getUser().getToken());
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

                repaymentTransactionAdapter.setLoading(true);
                repaymentTransactionAdapter.setPermitLoadMore(false);
                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                repaymentTransactionAdapter.setLoading(false);
                repaymentTransactionAdapter.setPermitLoadMore(true);
                if (onError && data == null) setLoading(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setTransaction(object.getJSONArray("repayments"), data != null);
                    state.putString("nextPage", object.getJSONObject("pagination").getString("nextPage"));
                    if (data == null) data = object;

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (data == null) setLoading(false);
                    Utility.toastMessage(LoanRepaymentTransactionsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(LoanRepaymentTransactionsActivity.this, object.getString("message"));
                    if (statusCode == 401) finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoanRepaymentTransactionsActivity.this, statusCode == 503 ? error :
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
            repayments.clear();
            if (!repaymentTransactionAdapter.isPermitLoadMore()) repaymentTransactionAdapter.setPermitLoadMore(true);
        }
    }

    private String getStateKey() {
        return STATE_KEY + "-" + applicationReference;
    }

    public Bundle getState() {
        return Utility.getState(getStateKey());
    }

    public Bundle getCurrentState() {

        if (state == null) state = getState();
        if (data != null) state.putString("data", data.toString());
        return state;
    }

    public void saveState() {
        Utility.saveState(getStateKey(), getCurrentState());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}