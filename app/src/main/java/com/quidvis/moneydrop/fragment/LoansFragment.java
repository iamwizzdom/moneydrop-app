package com.quidvis.moneydrop.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.android.material.tabs.TabLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanDetailsActivity;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.UserLoanActivity;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.adapter.LoanAdapter;
import com.quidvis.moneydrop.adapter.ViewPagerAdapter;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.User;
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

public class LoansFragment extends CustomCompatFragment {

    private View view;
    private FragmentActivity activity;
    private final ArrayList<CustomFragment> fragments = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.loans_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();

        if (fragments.size() <= 0) {
            LoanOffersFragment loanOffersFragment = LoanOffersFragment.newInstance();
            LoanRequestsFragment loanRequestsFragment = LoanRequestsFragment.newInstance();
            fragments.add(loanOffersFragment);
            fragments.add(loanRequestsFragment);
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter((AppCompatActivity) activity, fragments);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPagerAdapter.setTabLayout(tabs);

        TextView tv1 = getTextView();
        TextView tv2 = getTextView();
        tv1.setText(R.string.offers);
        tv2.setText(R.string.requests);

        Objects.requireNonNull(tabs.getTabAt(0)).setCustomView(tv1);
        Objects.requireNonNull(tabs.getTabAt(1)).setCustomView(tv2);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    View v = tab.getCustomView();
                    if (v != null) selectView(v);
                    viewPagerAdapter.getItem(tab.getPosition()).mount();
                    viewPagerAdapter.notifyDataSetChanged(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab != null) {
                    View v = tab.getCustomView();
                    if (v != null) deselectView(v);
                    viewPagerAdapter.getItem(tab.getPosition()).dismount();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabs.selectTab(tabs.getTabAt(0), true);
        selectView(Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getCustomView()));
        ((MainActivity) activity).setCustomTitle(activity.getResources().getString(R.string.loan_offers));
        ((MainActivity) activity).setCustomSubtitle(activity.getResources().getString(R.string.no_record));
    }

    private TextView getTextView() {
        TextView tv = new TextView(activity);

        tv.setTextAppearance(activity, R.style.text_view_style);
        tv.setTextColor(activity.getResources().getColor(R.color.colorAccent));

        int padding = Utility.getDip(activity, 10);
        int paddingSides = Utility.getDip(activity, 20);
        tv.setPadding(paddingSides, padding, paddingSides, padding);

        Typeface typeface = ResourcesCompat.getFont(activity, R.font.campton_medium);
        tv.setTypeface(typeface);
        tv.setTextSize(16);

        return tv;
    }

    private void selectView(View view) {
        view.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.layout_background_rounded, null));
        view.setBackgroundTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorWhite)));
    }

    private void deselectView(View view) {
        view.setBackground(null);
        view.setBackgroundTintList(null);
    }

    public static void revokeLoan(Fragment fragment, LoanAdapter loanAdapter, Loan loan, boolean detach) {

        User user = (new DbHelper(fragment.getContext())).getUser();

        HttpRequest httpRequest = new HttpRequest(fragment,
                String.format(URLContract.LOAN_REVOKE_URL, loan.getUuid()),
                Request.Method.POST, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", user.getToken());
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
                loanAdapter.fadeItem(loanAdapter.getPosition(), true);
                Utility.toastMessage(fragment.getContext(), "Revoking loan, please wait.");
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                loanAdapter.fadeItem(loanAdapter.getPosition(), false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        JSONObject respObj = object.getJSONObject("response");
                        double balance = respObj.getDouble("balance");
                        double availableBalance = respObj.getDouble("available_balance");

                        JSONObject loanObj = respObj.getJSONObject("loan");
                        Loan loan = new Loan(fragment.getContext(), loanObj);

                        if (detach) loanAdapter.removeItem(loanAdapter.getPosition());
                        else {
                            loanAdapter.setItem(loanAdapter.getPosition(), loan);
                        }

                        Bundle mainFragmentState = Utility.getState(MainFragment.STATE_KEY);
                        String mainFragmentStringData = mainFragmentState.getString("data");

                        if (mainFragmentStringData != null) {

                            JSONObject mainFragmentData = new JSONObject(mainFragmentStringData);

                            mainFragmentData.put("balance", balance);
                            mainFragmentData.put("available_balance", availableBalance);

                            JSONArray loans = mainFragmentData.getJSONArray("loans");
                            for (int i = 0; i < loans.length(); i++) {
                                if (loan.getUuid().equals(loans.getJSONObject(i).getString("uuid"))) {
                                    loans.put(i, loan.getLoanObject());
                                    break;
                                }
                            }

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

                    }

                    Utility.toastMessage(fragment.getContext(), object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(fragment.getContext(), "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(fragment.getContext(), object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(fragment.getContext(), statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };

        httpRequest.send();

    }

    @Override
    public void onPause() {
        for (CustomFragment fragment : fragments) {
            fragment.saveState();
            fragment.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        for (CustomFragment fragment : fragments) {
            fragment.saveState();
            fragment.onDestroyView();
        }
        super.onDestroyView();
    }
}