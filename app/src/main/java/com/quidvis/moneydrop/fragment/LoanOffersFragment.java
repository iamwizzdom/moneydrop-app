package com.quidvis.moneydrop.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.quidvis.moneydrop.activity.LoanDetailsActivity.LOAN_OBJECT_KEY;
import static com.quidvis.moneydrop.activity.LoanDetailsActivity.LOAN_POSITION_KEY;

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

    public static final int LOAN_OFFER_DETAILS_KEY = 120;

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

//        if (viewPagerAdapter != null) viewPagerAdapter.notifyDataSetChanged(getPosition());

        tvNoContent = view.findViewById(R.id.no_content);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getLoanOffers(null);
        });

        recyclerView = view.findViewById(R.id.loan_offers_list);

        loanAdapter = new LoanAdapter(recyclerView, this, loans);
        recyclerView.setAdapter(loanAdapter);
        loanAdapter.setOnLoadMoreListener(() -> {
            loanAdapter.setLoading(true);
            getLoanOffers(state.getString("nextPage"));
        });

        registerForContextMenu(recyclerView);
        setUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            Utility.toastMessage(activity, "Failed to capture new loan data.");
            return;
        }

        if (requestCode == LOAN_OFFER_DETAILS_KEY) {

            int loanKey = data.getIntExtra(LOAN_POSITION_KEY, 0);
            String loanString = data.getStringExtra(LOAN_OBJECT_KEY);

            if (loanString == null) {
                Utility.toastMessage(activity, "No loan passed");
                return;
            }

            try {
                JSONObject loanObject = new JSONObject(loanString);
                Loan loan = new Loan(activity, loanObject);
                loans.set(loanKey, loan);
                loanAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
                Utility.toastMessage(activity, "Invalid loan passed");
            }
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        activity.getMenuInflater().inflate(R.menu.loan_offer_option, menu);
        Log.e("onCreateContextMenu", "I ran onCreateContextMenu in " + this);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.revoke_loan_offer) {
            Loan loan = loanAdapter.getItem(loanAdapter.getPosition());
            if (loan != null) {
                CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog((AppCompatActivity) activity);
                alertDialog.setIcon(R.drawable.ic_remove);
                alertDialog.setMessage("Are you sure you want to revoke this loan?");
                alertDialog.setNegativeButton("No, cancel");
                alertDialog.setPositiveButton("Yes, proceed", vw -> LoansFragment.revokeLoan(activity, loanAdapter, loan, activity instanceof MainActivity));
                alertDialog.display();
            } else {
                Utility.toastMessage(activity, "Invalid item selected");
            }
        }
        return super.onContextItemSelected(item);
    }

    private void setUp() {

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
                setLoanOffers(data.getJSONArray("loans"), false);
            } catch (JSONException e) {
                getLoanOffers(Objects.requireNonNull(state).getString("nextPage"));
                e.printStackTrace();
            }

        } else {
            getLoanOffers(Objects.requireNonNull(state).getString("nextPage"));
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

    private void setLoanOffers(JSONArray loanOffers, boolean addUp) {

        int size = loanOffers.length();

        if (!addUp) this.loans.clear();

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

                JSONObject loanOffer = loanOffers.getJSONObject(i);
                Loan loan = new Loan(activity, loanOffer);
                loans.add(loan);
                if (!addUp) continue;
                data.getJSONArray("loans").put(loanOffer);

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

    private void getLoanOffers(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            loanAdapter.setLoading(false);
            loanAdapter.setPermitLoadMore(false);
            loans.add(null);
            loanAdapter.notifyItemInserted(loans.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity,
                nextPage != null ? URLContract.BASE_URL + nextPage : (activity instanceof UserLoanActivity ?
                        URLContract.USER_LOAN_OFFERS_LIST_URL : URLContract.LOAN_OFFERS_LIST_URL),
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

                if (isRefreshing()) setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setLoanOffers(object.getJSONArray("loans"), data != null);
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
            loans.clear();
            if (!loanAdapter.isPermitLoadMore()) loanAdapter.setPermitLoadMore(true);
        }
    }

    private String getStateKey() {
        String stateKey = activity instanceof MainActivity ? MainActivity.STATE_KEY : UserLoanActivity.STATE_KEY;
        stateKey += ("-" + STATE_KEY);
        return stateKey;
    }

    public Bundle getState() {
        return Utility.getState(getStateKey());
    }

    public Bundle getCurrentState() {
        if (state == null) state = getState();
        if (data != null) state.putString("data", data.toString());
        return state;
    }

    public void removeData(int index) {
        try {
            data.getJSONArray("loans").remove(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveState() {
        Utility.saveState(getStateKey(), getCurrentState());
    }

    @Override
    public void refresh() {
        if (data == null) getLoanOffers(null);
        else setUp();
    }

    @Override
    public void mount() {
        if (recyclerView != null) {
            registerForContextMenu(recyclerView);
            Log.e("mount", "mounted " + this);
        }
    }

    @Override
    public void dismount() {
        if (recyclerView != null) {
            unregisterForContextMenu(recyclerView);
            Log.e("dismount", "dismounted " + this);
        }
    }
}