package com.quidvis.moneydrop.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.adapter.TransactionAdapter;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.utility.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransactionsActivity extends AppCompatActivity {

    private final static String STATE_KEY = TransactionsActivity.class.getName();
    private DbHelper dbHelper;
    private TransactionAdapter transactionAdapter;
    private final ArrayList<Loan> loans = new ArrayList<>();
    private Bundle savedState;
    private JSONObject data;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvItemCount, tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        tvItemCount = findViewById(R.id.item_count);
        tvItemCount.setText(R.string.no_record);
        tvNoContent = findViewById(R.id.no_content);
        shimmerFrameLayout = findViewById(R.id.shimmer_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getTransactions(1);
        });

        recyclerView = findViewById(R.id.transaction_list);

        dbHelper = new DbHelper(this);

        savedState = getState();

        transactionAdapter = new TransactionAdapter(recyclerView, this, loans);
        recyclerView.setAdapter(transactionAdapter);
        transactionAdapter.setOnLoadMoreListener(() -> {
            transactionAdapter.setLoading(true);
            getTransactions(savedState.getInt("page", 1));
        });

        if (savedState != null && savedState.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(savedState.getString("data")));
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setTransaction(data.getJSONArray("transactions"), false);
            } catch (JSONException e) {
                getTransactions(Objects.requireNonNull(savedState).getInt("page", 1));
                e.printStackTrace();
            }

        } else {
            getTransactions(Objects.requireNonNull(savedState).getInt("page", 1));
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

    private void setTransaction(JSONArray loanRequests, boolean addUp) {

        int size = loanRequests.length();

        if (addUp && size <= 0) {

            if (transactionAdapter.isLoading()) {
                setLoading(false, true);
                transactionAdapter.setLoading(false);
                transactionAdapter.setPermitLoadMore(false);
                loans.add(null);
                transactionAdapter.notifyItemInserted(loans.size() - 1);
            }

            return;
        }

        if (transactionAdapter.isLoading()) transactionAdapter.setLoading(false);
        transactionAdapter.setPermitLoadMore(true);

        for (int i = 0; i < size; i++) {
            try {

                JSONObject loanRequest = loanRequests.getJSONObject(i);
                Loan loan = new Loan();
                loan.setId(loanRequest.getInt("id"));
                loan.setType(loanRequest.getString("type"));
                loan.setAmount(loanRequest.getDouble("amount"));
                loan.setStatus(loanRequest.getString("status"));
                loan.setDate(loanRequest.getString("date"));
                loans.add(loan);
                if (!addUp) continue;
                data.getJSONArray("transactions").put(loanRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = loans.size();
        tvItemCount.setText((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : getResources().getText(R.string.no_record));
        transactionAdapter.notifyDataSetChanged();
    }

    private void getTransactions(int page) {

        HttpRequest httpRequest = new HttpRequest(this,
                String.format("%s?page=%s", URLContract.TRANSACTION_LIST_URL, page),
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

                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setTransaction(object.getJSONArray("transactions"), data != null);
                    savedState.putInt("page", object.getInt("page"));
                    if (data == null) data = object;

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (data == null) setLoading(false);
                    Utility.toastMessage(TransactionsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(TransactionsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(TransactionsActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                if (data == null) setLoading(false);
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
            savedState.putBundle("data", null);
            savedState.putInt("page", 1);
            loans.clear();
            if (!transactionAdapter.isPermitLoadMore()) transactionAdapter.setPermitLoadMore(true);
        }
    }

    private Bundle getState() {
        return getState(null);
    }

    public Bundle getState(String key) {
        Bundle state = Utility.getState(STATE_KEY);
        return key != null ? state.getBundle(key) : state;
    }

    private void saveState(Bundle state) {
        saveState(null, state);
    }

    public void saveState(String key, Bundle state) {
        Bundle prevState;
        if (key != null) {
            prevState = Utility.getState(STATE_KEY);
            prevState.putBundle(key, state);
        } else prevState = state;
        Utility.saveState(STATE_KEY, prevState);
    }

    @Override
    protected void onDestroy() {
        Bundle bundle = getState();
        if (data != null) bundle.putString("data", data.toString());
        if (savedState != null) bundle.putInt("page", savedState.getInt("page"));
        saveState(bundle);
        super.onDestroy();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}