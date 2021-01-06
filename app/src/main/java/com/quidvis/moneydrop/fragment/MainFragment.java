package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
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

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.TransactionReceiptActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
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

public class MainFragment extends Fragment {

    public final static String STATE_KEY = MainFragment.class.getName();
    private Activity activity;
    private DbHelper dbHelper;
    private static Bundle state;
    private LinearLayout loanView, transactionView;
    private TextView loanEmpty, transactionEmpty;
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
                setBalance(data.getDouble("balance"));
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

                View view = getView(loan.getJSONObject(i), false);
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


                View view = getView(trans.getJSONObject(i), true);
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

    private View getView(JSONObject transaction, boolean isTransaction) throws JSONException {

        if (!this.isAdded()) return null;

        View view = getLayoutInflater().inflate(R.layout.transaction_layout, null);

        LinearLayout container = view.findViewById(R.id.container);
        ImageView mvIcon = view.findViewById(R.id.transaction_icon);
        TextView tvType = view.findViewById(R.id.transaction_type);
        TextView tvDate = view.findViewById(R.id.transaction_date);
        TextView tvAmount = view.findViewById(R.id.transaction_amount);
        TextView tvStatus = view.findViewById(R.id.transaction_status);

        ArrayMap<String, Integer> theme = getTheme(transaction.getString("status"));

        tvType.setText(transaction.getString("type"));
        tvDate.setText(transaction.getString("date"));
        tvAmount.setText(format.format(transaction.getDouble("amount")));
        tvStatus.setText(Utility.ucFirst(transaction.getString("status")));

        mvIcon.setImageDrawable(ContextCompat.getDrawable(activity, Objects.requireNonNull(theme.get("icon"))));
        tvAmount.setTextColor(activity.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(activity, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

        if (isTransaction) {
            container.setOnClickListener(v -> {
                Intent intent = new Intent(activity, TransactionReceiptActivity.class);
                intent.putExtra(TransactionReceiptActivity.TRANSACTION_KEY, transaction.toString());
                activity.startActivity(intent);
            });
        }

        return view;
    }

    public void setLoadingLoans(boolean status) {
        setLoadingLoans(status, false);
    }

    public void setLoadingLoans(boolean status, boolean hasContent) {
        if (status) {

            loanEmpty.setVisibility(View.GONE);
            loanShimmerFrameLayout.setVisibility(View.VISIBLE);
            loanShimmerFrameLayout.startShimmer();

        } else {

            loanShimmerFrameLayout.stopShimmer();
            loanShimmerFrameLayout.setVisibility(View.GONE);
            loanEmpty.setVisibility(hasContent ? View.GONE : View.VISIBLE);
        }
    }

    public void setLoadingTransactions(boolean status) {
        setLoadingTransactions(status, false);
    }

    public void setLoadingTransactions(boolean status, boolean hasContent) {
        if (status) {

            transactionEmpty.setVisibility(View.GONE);
            transactionShimmerFrameLayout.setVisibility(View.VISIBLE);
            transactionShimmerFrameLayout.startShimmer();

        } else {

            transactionShimmerFrameLayout.stopShimmer();
            transactionShimmerFrameLayout.setVisibility(View.GONE);
            transactionEmpty.setVisibility(hasContent ? View.GONE : View.VISIBLE);
        }
    }

    private void getDashboardData() {

        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity, URLContract.DASHBOARD_REQUEST_URL, Request.Method.GET, new HttpRequestParams() {
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

                setLoadingLoans(true);
                setLoadingTransactions(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    data = new JSONObject(response);

                    setBalance(data.getDouble("balance"));
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
        Utility.saveState(STATE_KEY, getCurrentState());
        super.onDestroyView();
    }
}