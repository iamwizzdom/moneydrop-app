package com.quidvis.moneydrop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.UserLoanActivity;
import com.quidvis.moneydrop.adapter.LoanAdapter;
import com.quidvis.moneydrop.adapter.ViewPagerAdapter;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoanOffersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoanOffersFragment extends CustomFragment {

    // TODO: Rename parameter arguments, choose names that match

    private FragmentActivity activity;
    private DbHelper dbHelper;
    private LoanAdapter loanAdapter;
    private ViewPagerAdapter viewPagerAdapter;
    private final ArrayList<Loan> loans = new ArrayList<>();

    private final static String STATE_KEY = LoanOffersFragment.class.getName();
    private Bundle state = null;
    private JSONObject data;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    public LoanOffersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoanOffersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoanOffersFragment newInstance() {
        return new LoanOffersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.loan_offers));
        setSubtitle(getResources().getString(R.string.no_record));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loan_offers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();
        dbHelper = new DbHelper(activity);
        viewPagerAdapter = getViewPagerAdapter();

        if (activity instanceof MainActivity) state = ((MainActivity) activity).getState(STATE_KEY);
        else if (activity instanceof UserLoanActivity) state = ((UserLoanActivity) activity).getState(STATE_KEY);

//        if (viewPagerAdapter != null) viewPagerAdapter.notifyDataSetChanged(getPosition());

        tvNoContent = view.findViewById(R.id.no_content);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getLoanOffers(1);
        });

        recyclerView = view.findViewById(R.id.loan_offers_list);

        loanAdapter = new LoanAdapter(recyclerView, activity, loans);
        recyclerView.setAdapter(loanAdapter);
        loanAdapter.setOnLoadMoreListener(() -> {
            loanAdapter.setLoading(true);
            getLoanOffers(state.getInt("page", 1));
        });

        if (state != null && state.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(state.getString("data")));
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setLoanOffers(data.getJSONArray("loan_offers"), false);
            } catch (JSONException e) {
                getLoanOffers(Objects.requireNonNull(state).getInt("page", 1));
                e.printStackTrace();
            }

        } else {
            getLoanOffers(Objects.requireNonNull(state).getInt("page", 1));
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

    private void setLoanOffers(JSONArray loanRequests, boolean addUp) {

        int size = loanRequests.length();

        if (addUp && size <= 0) {

            if (loanAdapter.isLoading()) {
                setLoading(false, true);
                loanAdapter.setLoading(false);
                loanAdapter.setPermitLoadMore(false);
                loans.add(null);
                loanAdapter.notifyItemInserted(loans.size() - 1);
            }

            return;
        }

        if (loanAdapter.isLoading()) loanAdapter.setLoading(false);
        loanAdapter.setPermitLoadMore(true);

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
                data.getJSONArray("loan_offers").put(loanRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = loans.size();
        setSubtitle((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : activity.getResources().getString(R.string.no_record));
        loanAdapter.notifyDataSetChanged();
        if (viewPagerAdapter != null) viewPagerAdapter.notifyDataSetChanged(getPosition());
    }

    private void getLoanOffers(int page) {

        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity,
                String.format("%s?page=%s", URLContract.LOAN_OFFERS_LIST_URL, page),
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
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setLoanOffers(object.getJSONArray("loan_offers"), data != null);
                    state.putInt("page", object.getInt("page"));
                    if (data == null) data = object;

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (data == null) setLoading(false);
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
            state.putBundle("data", null);
            state.putInt("page", 1);
            loans.clear();
            if (!loanAdapter.isPermitLoadMore()) loanAdapter.setPermitLoadMore(true);
        }
    }

    public Bundle getCurrentState() {
        if (data != null) state.putString("data", data.toString());
        return state;
    }

    @Override
    public void saveState() {
        if (isAdded()) MainActivity.saveState(STATE_KEY, getCurrentState());
    }

    @Override
    public void refresh() {
        if (data == null) getLoanOffers(Objects.requireNonNull(state).getInt("page", 1));
    }
}