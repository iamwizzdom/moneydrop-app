 package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.cottacush.android.currencyedittext.CurrencyEditText;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.view.DialogSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class OfferLoanFragment extends CustomFragment {

    private Activity activity;
    private User user;
    private Session session;
    private TextView tvInterestRate;
    private CurrencyEditText etAmount;
    private DialogSpinner dsInterestType, dsLoanTenor;
    private CircularProgressButton submitBtn;
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
    private int interestRate;
    private String[] interestTypeKeys;
    private String[] loanTenorKeys;
    private final int incrementer = 500;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_offer_loan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = requireActivity();
        format.setMaximumFractionDigits(0);

        DbHelper dbHelper = new DbHelper(activity);
        user = dbHelper.getUser();
        session = new Session(activity);

        LinearLayout amountList = view.findViewById(R.id.amount_list);
        tvInterestRate = view.findViewById(R.id.tvInterestRate);
        dsLoanTenor = view.findViewById(R.id.loan_tenor);
        dsInterestType = view.findViewById(R.id.interest_type);
        etAmount = view.findViewById(R.id.amount);
        submitBtn = view.findViewById(R.id.submit);
        SeekBar seekBar = view.findViewById(R.id.seekBar);

        ImageView ivIncrement = view.findViewById(R.id.ivIncrement);
        ImageView ivDecrement = view.findViewById(R.id.ivDecrement);

        ivIncrement.setOnClickListener(v -> {
            double amount = etAmount.getNumericValue();
            etAmount.setText(String.valueOf((int) (amount + incrementer)));
        });

        ivDecrement.setOnClickListener(v -> {
            double amount = etAmount.getNumericValue();
            if (amount > incrementer) amount = (amount - incrementer);
            else amount = 0;
            etAmount.setText(String.valueOf((int) amount));
        });

        setInterestRate(seekBar.getProgress());

        int[] amounts = new int[]{10000, 25000, 50000, 100000, 200000, 300000, 400000,
                500000, 600000, 700000, 800000, 900000, 1000000};

        for (int i = 0; i < amounts.length; i++) {
            TextView tv = new TextView(activity);

            tv.setTextAppearance(activity, R.style.text_view_style);
            tv.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.layout_background_rounded, null));
            tv.setBackgroundTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorGrayExtraLight)));
            tv.setTextColor(activity.getResources().getColor(R.color.colorAccent));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            if (i > 0) params.leftMargin = Utility.getDip(activity, 10);
            tv.setLayoutParams(params);

            int padding = Utility.getDip(activity, 20);
            tv.setPadding(padding, padding, padding, padding);
            Typeface typeface = ResourcesCompat.getFont(activity, R.font.campton_medium);
            tv.setTypeface(typeface);

            tv.setTextSize(16);
            tv.setText(format.format(amounts[i]));
            tv.setTag(amounts[i]);
            tv.setOnClickListener(v -> etAmount.setText(String.valueOf((int) v.getTag())));
            amountList.addView(tv);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                setInterestRate(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        JSONObject loanConsts = session.getJSONObject("loan_consts");
        if (loanConsts == null) getLoanConst();
        else {
            try {
                setLoanTenor(loanConsts.getJSONObject("tenor"));
                setInterestType(loanConsts.getJSONObject("interest_type"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        submitBtn.setOnClickListener(this::submitLoanOffer);
    }

    private void setInterestType(JSONObject interestType) {
        interestTypeKeys = new String[interestType.length()];
        int i = 0;
        String[] interestTypes = new String[interestType.length()];
        for (Iterator<String> it = interestType.keys(); it.hasNext(); i++) {
            interestTypeKeys[i] = it.next();
            try {
                interestTypes[i] = interestType.getString(interestTypeKeys[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dsInterestType.setEntries(interestTypes);
    }

    private void setLoanTenor(JSONObject loanTenor) {
        loanTenorKeys = new String[loanTenor.length()];
        int i = 0;
        String[] loanTenors = new String[loanTenor.length()];
        for (Iterator<String> it = loanTenor.keys(); it.hasNext(); i++) {
            loanTenorKeys[i] = it.next();
            try {
                loanTenors[i] = loanTenor.getString(loanTenorKeys[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dsLoanTenor.setEntries(loanTenors);
    }

    private void setInterestRate(int interestRate) {
        this.interestRate = interestRate;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(interestRate + "% in return");
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.3f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvInterestRate.setText(spannableStringBuilder);
    }

    private void submitLoanOffer(View view) {
        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity,
                URLContract.LOAN_OFFER_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf(etAmount.getNumericValue()));
                params.put("interest", String.valueOf(OfferLoanFragment.this.interestRate));
                if (dsInterestType.isSelected()) params.put("interest_type", interestTypeKeys[dsInterestType.getSelectedItemPosition()]);
                if (dsLoanTenor.isSelected()) params.put("tenor", loanTenorKeys[dsLoanTenor.getSelectedItemPosition()]);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", String.format("Bearer %s", user.getToken()));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                submitBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                submitBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject data = new JSONObject(response);

                    if (data.getBoolean("status")) {

                        JSONArray transactions; int size;
                        double balance = data.getJSONObject("response").getDouble("balance");
                        JSONObject loan = data.getJSONObject("response").getJSONObject("loan");

                        if (loan.has("transaction") && !Utility.isEmpty(loan.getString("transaction")).isEmpty()) {

                            JSONObject transaction = loan.getJSONObject("transaction");

                            Bundle mainFragmentState = Utility.getState(MainFragment.STATE_KEY);
                            String mainFragmentStringData = mainFragmentState.getString("data");

                            if (mainFragmentStringData != null) {

                                JSONObject mainFragmentData = new JSONObject(mainFragmentStringData);

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("transactions", Utility.prependJSONObject(transactions, transaction));

                                mainFragmentState.putString("data", mainFragmentData.toString());
                                Utility.saveState(MainFragment.STATE_KEY, mainFragmentState);
                            }

                            Bundle walletFragmentState = Utility.getState(WalletFragment.STATE_KEY);
                            String walletFragmentStringData = walletFragmentState.getString("data");

                            if (walletFragmentStringData != null) {

                                JSONObject walletFragmentData = new JSONObject(walletFragmentStringData);

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("transactions", Utility.prependJSONObject(transactions, transaction));

                                walletFragmentState.putString("data", walletFragmentData.toString());
                                Utility.saveState(WalletFragment.STATE_KEY, walletFragmentState);
                            }

                        }

                        Fragment fragment = getParentFragment();
                        if (fragment != null) {
                            fragment = fragment.getParentFragment();
                            if (fragment != null) {
                                fragment = fragment.getParentFragment();
                                if (fragment != null) {
                                    List<Fragment> fragments = fragment.getChildFragmentManager().getFragments();
                                    for (Fragment frag: fragments) {
                                        if (frag instanceof OfferLoanCentralFragment) {
                                            ((OfferLoanCentralFragment) frag).loadFragment(view, String.valueOf(etAmount.getNumericValue()), data.getString("message"));
                                        }
                                    }
                                }
                            }
                        }

                    } else {

                        Utility.toastMessage(activity, data.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(activity);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("error") && object.getJSONObject("error").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("error"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", Dialog::dismiss);

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void getLoanConst() {
        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity,
                URLContract.LOAN_CONSTANTS_URL, Request.Method.GET, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", String.format("Bearer %s", user.getToken()));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {

            }

            @Override
            protected void onRequestCompleted(boolean onError) {
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject data = new JSONObject(response);
                    session.setJSONObject("loan_consts", data);
                    setLoanTenor(data.getJSONObject("tenor"));
                    setInterestType(data.getJSONObject("interest_type"));

                } catch (JSONException e) {
                    e.printStackTrace();
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
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    @Override
    public void onResume() {
        super.onResume();
        etAmount.setText("");
    }

    @Override
    public void saveState() {

    }

    @Override
    public void refresh() {

    }
}