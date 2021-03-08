package com.quidvis.moneydrop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanApplicationDetailsActivity;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.adapter.HistoryAdapter;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends CustomCompatFragment {

    // TODO: Rename parameter arguments, choose names that match

    private FragmentActivity activity;
    private DbHelper dbHelper;
    private HistoryAdapter historyAdapter;
    private final ArrayList<LoanApplication> loanApplications = new ArrayList<>();

    private final static String STATE_KEY = HistoryFragment.class.getName();
    private Bundle state = null;
    private JSONObject data;
    private static boolean started = false;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    public static final int LOAN_HISTORY_REQUEST_KEY = 123;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoanOffersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();
        dbHelper = new DbHelper(activity);

        tvNoContent = view.findViewById(R.id.no_content);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getHistory(null);
        });

        recyclerView = view.findViewById(R.id.history_list);

        historyAdapter = new HistoryAdapter(recyclerView, this, loanApplications);
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.setOnLoadMoreListener(() -> {
            historyAdapter.setLoading(true);
            getHistory(state.getString("nextPage"));
        });

        ((MainActivity) activity).setCustomTitle(getResources().getString(R.string.history));
        ((MainActivity) activity).setCustomSubtitle(getResources().getString(R.string.no_record));

        setUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            Utility.toastMessage(activity, "Failed to capture new history data.");
            return;
        }

        if (requestCode == LOAN_HISTORY_REQUEST_KEY) {

            int historyKey = data.getIntExtra(LoanApplicationDetailsActivity.LOAN_APPLICATION_POSITION_KEY, 0);
            String historyString = data.getStringExtra(LoanApplicationDetailsActivity.LOAN_APPLICATION_OBJECT_KEY);

            if (historyString == null) {
                Utility.toastMessage(activity, "No history data passed");
                return;
            }

            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                try {

                    JSONObject historyObject = new JSONObject(historyString);
                    historyObject.put("amount", historyObject.getDouble("amount") + 100);
                    LoanApplication loanApplication = new LoanApplication(activity, historyObject);
                    loanApplications.set(historyKey, loanApplication);
                    historyAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, "Invalid history passed");
                }

            }, 200);

        } else Utility.toastMessage(activity, "Invalid request code");

    }

    private void setUp() {

        if (started) return;

        started = true;

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
                setHistory(data.getJSONArray("applications"), false);
            } catch (JSONException e) {
                getHistory(Objects.requireNonNull(state).getString("nextPage"));
                e.printStackTrace();
            }

        } else {
            getHistory(Objects.requireNonNull(state).getString("nextPage"));
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
    }

    private void setHistory(JSONArray applications, boolean addUp) {

        int size = applications.length();

        if (!addUp) this.loanApplications.clear();

        if (addUp && size <= 0) {

            if (historyAdapter.isLoading()) {
                setLoading(false, true);
                historyAdapter.setLoading(false);
                historyAdapter.setPermitLoadMore(false);
                this.loanApplications.add(null);
                historyAdapter.notifyItemInserted(this.loanApplications.size() - 1);
            }

            return;
        }

        if (historyAdapter.isLoading()) historyAdapter.setLoading(false);
        historyAdapter.setPermitLoadMore(true);

        for (int i = 0; i < size; i++) {
            try {

                LoanApplication application = new LoanApplication(activity, applications.getJSONObject(i));
                this.loanApplications.add(application);
                if (!addUp) continue;
                data.getJSONArray("applications").put(application.getApplicationObject());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = this.loanApplications.size();
        ((MainActivity) activity).setCustomSubtitle((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : activity.getResources().getString(R.string.no_record));
        historyAdapter.notifyDataSetChanged();
    }

    private void getHistory(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            historyAdapter.setLoading(false);
            historyAdapter.setPermitLoadMore(false);
            loanApplications.add(null);
            historyAdapter.notifyItemInserted(loanApplications.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this,
                nextPage != null ? (URLContract.BASE_URL + nextPage) : URLContract.HISTORY_URL,
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

                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                if (isRefreshing()) setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setHistory(object.getJSONArray("applications"), data != null);
                    state.putString("nextPage", object.getJSONObject("pagination").getString("nextPage"));
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
            state.putString("nextPage", null);
            loanApplications.clear();
            if (!historyAdapter.isPermitLoadMore()) historyAdapter.setPermitLoadMore(true);
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
    public void onPause() {
        super.onPause();
        started = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        started = false;
        saveState();
    }
}