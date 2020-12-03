package com.quidvis.moneydrop.fragment;

import android.app.Activity;
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
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.utility.HttpRequest;
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

    private final static String STATE_KEY = MainFragment.class.getName();
    private Activity activity;
    private DbHelper dbHelper;
    private LinearLayout loanRequestView, transactionView;
    private TextView loanRequestEmpty, transactionEmpty;
    ShimmerFrameLayout loanRequestShimmerFrameLayout, transactionShimmerFrameLayout;
    private JSONObject data;

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

        loanRequestView = view.findViewById(R.id.loan_request_list);
        transactionView = view.findViewById(R.id.transaction_list);
        loanRequestEmpty = view.findViewById(R.id.loan_request_empty);
        transactionEmpty = view.findViewById(R.id.transaction_empty);

        loanRequestShimmerFrameLayout = view.findViewById(R.id.loan_request_shimmer_view);
        transactionShimmerFrameLayout = view.findViewById(R.id.transaction_shimmer_view);

        ((MainActivity) activity).setCustomSubtitle("Available balance");

        Bundle savedState = getState();

        if(savedState != null && savedState.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(savedState.getString("data")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setBalance(data.getDouble("balance"));
                setLoanRequest(data.getJSONArray("loan_requests"));
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

    private void setBalance(double amount) {
        format.setMaximumFractionDigits(2);
        ((MainActivity) activity).setCustomTitle(format.format(amount));
        format.setMaximumFractionDigits(0);
    }

    private void setLoanRequest(JSONArray loan) {

        int size = loan.length();

        for (int i = 0; i < size; i++) {
            try {

                View view = getView(loan.getJSONObject(i));
                if (view == null) continue;
                if ((i == 0 && size > 1) || i > 0 && i < (size - 1))
                    view.setBackgroundResource(R.drawable.layout_underline);
                loanRequestView.addView(view);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoadingLoanRequest(false, size > 0);
    }

    private void setTransactions(JSONArray trans) {

        int size = trans.length();

        for (int i = 0; i < size; i++) {
            try {

                View view = getView(trans.getJSONObject(i));
                if (view != null) {
                    if ((i == 0 && size > 1) || i > 0 && i < (size - 1)) view.setBackgroundResource(R.drawable.layout_underline);
                    transactionView.addView(view);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoadingTransactions(false, size > 0);
    }

    private View getView(JSONObject transaction) throws JSONException {

        if (!this.isAdded()) return null;

        View view = getLayoutInflater().inflate(R.layout.transaction_layout, null);

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

        return view;
    }

    public void setLoadingLoanRequest(boolean status) {
        setLoadingLoanRequest(status, false);
    }

    public void setLoadingLoanRequest(boolean status, boolean hasContent) {
        if (status) {

            loanRequestEmpty.setVisibility(View.GONE);
            loanRequestShimmerFrameLayout.setVisibility(View.VISIBLE);
            loanRequestShimmerFrameLayout.startShimmer();

        } else {

            loanRequestShimmerFrameLayout.stopShimmer();
            loanRequestShimmerFrameLayout.setVisibility(View.GONE);
            loanRequestEmpty.setVisibility(hasContent ? View.GONE : View.VISIBLE);
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

                setLoadingLoanRequest(true);
                setLoadingTransactions(true);
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {

                try {

                    data = new JSONObject(response);

                    setBalance(data.getDouble("balance"));
                    setLoanRequest(data.getJSONArray("loan_requests"));
                    setTransactions(data.getJSONArray("transactions"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    setLoadingLoanRequest(false);
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

                setLoadingLoanRequest(false);
                setLoadingTransactions(false);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();

    }

    private Bundle getState() {
        return ((MainActivity) activity).getState(STATE_KEY);
    }

    private Bundle getCurrentState() {
        Bundle bundle = new Bundle();
        if (data != null) bundle.putString("data", data.toString());
        return bundle;
    }

    @Override
    public void onPause() {
        ((MainActivity) activity).saveState(STATE_KEY, getCurrentState());
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) activity).saveState(STATE_KEY, getCurrentState());
        super.onDestroyView();
    }
}