package com.quidvis.moneydrop.activity;

import android.os.Bundle;
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
import com.quidvis.moneydrop.model.Transaction;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransactionsActivity extends AppCompatActivity {

    public final static String STATE_KEY = TransactionsActivity.class.getName();
    private DbHelper dbHelper;
    private TransactionAdapter transactionAdapter;
    private final ArrayList<Transaction> transactions = new ArrayList<>();
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
        setContentView(R.layout.activity_transaction);

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

        transactionAdapter = new TransactionAdapter(recyclerView, this, transactions);
        recyclerView.setAdapter(transactionAdapter);
        transactionAdapter.setOnLoadMoreListener(() -> getTransactions(state.getString("nextPage")));

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
                setTransaction(data.getJSONArray("transactions"), false);
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

    private void setTransaction(JSONArray transactions, boolean addUp) {

        int size = transactions.length();

        if (!addUp) this.transactions.clear();

        for (int i = 0; i < size; i++) {
            try {

                JSONObject transObject = transactions.getJSONObject(i);
                Transaction transaction = new Transaction(this, transObject);
                this.transactions.add(transaction);
                if (!addUp) continue;
                data.getJSONArray("transactions").put(transObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = this.transactions.size();
        tvItemCount.setText((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : getResources().getText(R.string.no_record));
        transactionAdapter.notifyDataSetChanged();
    }

    private void getTransactions(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            transactionAdapter.setLoading(false);
            transactionAdapter.setPermitLoadMore(false);
            transactions.add(null);
            transactionAdapter.notifyItemInserted(transactions.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this,
                nextPage != null ? URLContract.BASE_URL + nextPage : URLContract.TRANSACTION_LIST_URL,
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

                transactionAdapter.setLoading(true);
                transactionAdapter.setPermitLoadMore(false);
                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                transactionAdapter.setLoading(false);
                transactionAdapter.setPermitLoadMore(true);
                if (onError && data == null) setLoading(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setTransaction(object.getJSONArray("transactions"), data != null);
                    state.putString("nextPage", object.getJSONObject("pagination").getString("nextPage"));
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
            transactions.clear();
            if (!transactionAdapter.isPermitLoadMore()) transactionAdapter.setPermitLoadMore(true);
        }
    }

    public Bundle getState() {
        return Utility.getState(STATE_KEY);
    }

    public Bundle getCurrentState() {
        if (state == null) state = getState();
        if (data != null) state.putString("data", data.toString());
        return state;
    }

    public void saveState() {
        Utility.saveState(STATE_KEY, getCurrentState());
    }

    @Override
    protected void onDestroy() {
        saveState();
        super.onDestroy();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}