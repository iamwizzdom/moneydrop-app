package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanDetailsActivity;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.TransactionReceiptActivity;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.Transaction;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.quidvis.moneydrop.utility.Utility.getTheme;

public class MainFragment extends CustomCompatFragment {

    public final static String STATE_KEY = MainFragment.class.getName();
    private Activity activity;
    private DbHelper dbHelper;
    private Bundle state;
    private LinearLayout loanView, transactionView;
    private TextView loanEmpty, transactionEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    ShimmerFrameLayout loanShimmerFrameLayout, transactionShimmerFrameLayout;
    public JSONObject data;
    private static boolean started = false;

    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();

        dbHelper = new DbHelper(activity);

        loanView = view.findViewById(R.id.loan_list);
        transactionView = view.findViewById(R.id.transaction_list);
        loanEmpty = view.findViewById(R.id.loan_request_empty);
        transactionEmpty = view.findViewById(R.id.transaction_empty);

        loanShimmerFrameLayout = view.findViewById(R.id.loan_request_shimmer_view);
        transactionShimmerFrameLayout = view.findViewById(R.id.transaction_shimmer_view);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            getDashboardData();
        });

        ((MainActivity) activity).setCustomSubtitle("Available balance");

        start();
    }

    private void start() {

        if (started) return;

        started = true;

        state = getState();

        if(state.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(state.getString("data")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setBalance(data.getDouble("available_balance"));
                setLoans(data.getJSONArray("loans"));
                setTransactions(data.getJSONArray("transactions"));
            } catch (JSONException e) {
                setBalance(0);
                getDashboardData();
                e.printStackTrace();
            }

        } else {
            setBalance(0);
            getDashboardData();
        }
    }

    public void setBalance(double amount) {
        format.setMaximumFractionDigits(2);
        ((MainActivity) activity).setCustomTitle(format.format(amount));
        format.setMaximumFractionDigits(0);
    }

    private void setLoans(JSONArray loan) {

        int size = loan.length();
        loanView.removeAllViews();

        for (int i = 0; i < size; i++) {
            try {

                View view = getLoanView(loan.getJSONObject(i));
                if (view == null) continue;
                if ((i == 0 && size > 1) || i > 0 && i < (size - 1))
                    view.setBackgroundResource(R.drawable.layout_underline);
                loanView.addView(view);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoadingLoans(false, size > 0);
    }

    public void setTransactions(JSONArray trans) {

        int size = Math.min(trans.length(), 2);
        transactionView.removeAllViews();

        for (int i = 0; i < size; i++) {
            try {

                View view = getTransactionView(trans.getJSONObject(i));
                if (view == null) continue;
                if ((i == 0 && size > 1) || i > 0 && i < (size - 1))
                    view.setBackgroundResource(R.drawable.layout_underline);
                transactionView.addView(view);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoadingTransactions(false, size > 0);
    }

    private View getLoanView(JSONObject object) throws JSONException {

        if (!this.isAdded()) return null;

        View view = getLayoutInflater().inflate(R.layout.loan_layout, null);

        Loan loan = new Loan(activity, object);

        LinearLayout container = view.findViewById(R.id.container);
        ImageView mvIcon = view.findViewById(R.id.profile_pic);
        TextView tvType = view.findViewById(R.id.loan_type);
        TextView tvDate = view.findViewById(R.id.loan_date);
        TextView tvAmount = view.findViewById(R.id.loan_amount);
        TextView tvStatus = view.findViewById(R.id.loan_status);

        tvType.setText(String.format("Loan %s (Me)", loan.getLoanType()));
        tvDate.setText(loan.getDate());
        tvAmount.setText(format.format(loan.getAmount()));
        tvStatus.setText(Utility.ucFirst(loan.getStatus()));

        User loanOwner = loan.getUser();

        Glide.with(activity)
                .load(loanOwner.getPictureUrl())
                .placeholder(loanOwner.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(loanOwner.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvIcon);

        ArrayMap<String, Integer> theme = getTheme(loan.getStatus());

        tvAmount.setTextColor(activity.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(activity, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

        container.setOnClickListener(v -> {
            Intent intent = new Intent(activity, LoanDetailsActivity.class);
            intent.putExtra(LoanDetailsActivity.LOAN_OBJECT_KEY, object.toString());
            activity.startActivity(intent);
        });

        return view;
    }

    private View getTransactionView(JSONObject object) throws JSONException {

        if (!this.isAdded()) return null;

        View view = getLayoutInflater().inflate(R.layout.transaction_layout, null);

        Transaction transaction = new Transaction(activity, object);

        LinearLayout container = view.findViewById(R.id.container);
        ImageView mvIcon = view.findViewById(R.id.transaction_icon);
        TextView tvType = view.findViewById(R.id.transaction_type);
        TextView tvDate = view.findViewById(R.id.transaction_date);
        TextView tvAmount = view.findViewById(R.id.transaction_amount);
        TextView tvStatus = view.findViewById(R.id.transaction_status);

        String type = transaction.getType();

        if (type.equals("offer") || type.equals("request")) {
            type = String.format("Loan %s (Me)", type);
        }

        tvType.setText(type);
        tvDate.setText(transaction.getDate());
        tvAmount.setText(format.format(transaction.getAmount()));
        tvStatus.setText(Utility.ucFirst(transaction.getStatus()));

        ArrayMap<String, Integer> theme = getTheme(transaction.getStatus(), transaction.getType().toLowerCase().equals("top-up"));

        mvIcon.setImageDrawable(ContextCompat.getDrawable(activity, Objects.requireNonNull(theme.get("icon"))));
        tvAmount.setTextColor(activity.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(activity, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

        container.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TransactionReceiptActivity.class);
            intent.putExtra(TransactionReceiptActivity.TRANSACTION_KEY, object.toString());
            activity.startActivity(intent);
        });

        return view;
    }

    public void setLoadingLoans(boolean status) {
        setLoadingLoans(status, false);
    }

    public void setLoadingLoans(boolean status, boolean hasContent) {
        if (status) {

            loanEmpty.setVisibility(View.GONE);
            loanView.setVisibility(View.GONE);
            loanShimmerFrameLayout.setVisibility(View.VISIBLE);
            loanShimmerFrameLayout.startShimmer();
        } else {

            loanShimmerFrameLayout.stopShimmer();
            loanShimmerFrameLayout.setVisibility(View.GONE);
            loanEmpty.setVisibility(hasContent ? View.GONE : View.VISIBLE);
            loanView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        }
    }

    public void setLoadingTransactions(boolean status) {
        setLoadingTransactions(status, false);
    }

    public void setLoadingTransactions(boolean status, boolean hasContent) {
        if (status) {

            transactionEmpty.setVisibility(View.GONE);
            transactionView.setVisibility(View.GONE);
            transactionShimmerFrameLayout.setVisibility(View.VISIBLE);
            transactionShimmerFrameLayout.startShimmer();

        } else {

            transactionShimmerFrameLayout.stopShimmer();
            transactionShimmerFrameLayout.setVisibility(View.GONE);
            transactionEmpty.setVisibility(hasContent ? View.GONE : View.VISIBLE);
            transactionView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        }
    }

    private void getDashboardData() {

        HttpRequest httpRequest = new HttpRequest(this, URLContract.DASHBOARD_REQUEST_URL,
                Request.Method.GET, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("JWT_AUTH", dbHelper.getUser().getToken());
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

                setLoadingLoans(true);
                setLoadingTransactions(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    data = new JSONObject(response);

                    setBalance(data.getDouble("available_balance"));
                    setLoans(data.getJSONArray("loans"));
                    setTransactions(data.getJSONArray("transactions"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    setLoadingLoans(false);
                    setLoadingTransactions(false);
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);

                    Utility.toastMessage(activity, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                setLoadingLoans(false);
                setLoadingTransactions(false);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();

    }

    public JSONObject getData() {
        return data;
    }

    public Bundle getState() {
        return Utility.getState(STATE_KEY);
    }

    public Bundle getCurrentState() {
        if (data != null) state.putString("data", data.toString());
        return state;
    }

    public void saveState() {
        Utility.saveState(STATE_KEY, getCurrentState());
    }

    @Override
    public void onPause() {
        super.onPause();
        started = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onDestroyView() {
        saveState();
        super.onDestroyView();
    }
}