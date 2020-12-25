package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.MainFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.BankAccount;
import com.quidvis.moneydrop.model.Card;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class WalletActivity extends AppCompatActivity {

    private final static String STATE_KEY = WalletActivity.class.getName();
    private DbHelper dbHelper;
    private User user;
    private LinearLayout transactionView;
    private TextView transactionEmpty, walletAmount;
    private SwipeRefreshLayout swipeRefreshLayout;
    ShimmerFrameLayout transactionShimmerFrameLayout;
    public static JSONObject data;
    private Bundle savedState;

    private float topUpAmount = 0, cashOutAmount = 0;
    private final int MIN_TOP_UP_AMOUNT = 1000;

    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        dbHelper = new DbHelper(WalletActivity.this);
        user = dbHelper.getUser();

        format.setMaximumFractionDigits(2);

        walletAmount = findViewById(R.id.wallet_amount);
        transactionView = findViewById(R.id.transaction_list);
        transactionEmpty = findViewById(R.id.transaction_empty);
        transactionShimmerFrameLayout = findViewById(R.id.transaction_shimmer_view);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            getData();
        });

        start();
    }

    private void start() {

        savedState = getState();

        if(savedState.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(savedState.getString("data")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setBalance(data.getDouble("balance"));
                setTransactions(data.getJSONArray("transactions"));
            } catch (JSONException e) {
                setBalance(0);
                getData();
                e.printStackTrace();
            }

        } else {
            setBalance(0);
            getData();
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

    private void setTransactions(JSONArray trans) {

        int size = trans.length();
        transactionView.removeAllViews();

        for (int i = 0; i < size; i++) {
            try {

                View view = getView(trans.getJSONObject(i));
                if ((i == 0 && size > 1) || i > 0 && i < (size - 1)) view.setBackgroundResource(R.drawable.layout_underline);
                transactionView.addView(view);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoadingTransactions(false, size > 0);
    }

    private View getView(JSONObject transaction) throws JSONException {

        View view = getLayoutInflater().inflate(R.layout.transaction_layout, null);

        ImageView mvIcon = view.findViewById(R.id.transaction_icon);
        TextView tvType = view.findViewById(R.id.transaction_type);
        TextView tvDate = view.findViewById(R.id.transaction_date);
        TextView tvAmount = view.findViewById(R.id.transaction_amount);
        TextView tvStatus = view.findViewById(R.id.transaction_status);

        ArrayMap<String, Integer> theme = Utility.getTheme(transaction.getString("status"));

        tvType.setText(transaction.getString("type"));
        tvDate.setText(transaction.getString("date"));
        tvAmount.setText(format.format(transaction.getDouble("amount")));
        tvStatus.setText(Utility.ucFirst(transaction.getString("status")));

        mvIcon.setImageDrawable(ContextCompat.getDrawable(this, Objects.requireNonNull(theme.get("icon"))));
        tvAmount.setTextColor(getResources().getColor(Objects.requireNonNull(theme.get("color"))));
        tvStatus.setTextAppearance(this, Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

        return view;
    }

    private void setBalance(double amount) {
        walletAmount.setText(format.format(amount));
        try {
            if (MainFragment.data != null) MainFragment.data.put("balance", amount);
            if (WalletActivity.data != null) {
                WalletActivity.data.put("balance", amount);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getData() {

        HttpRequest httpRequest = new HttpRequest(this,
                URLContract.DASHBOARD_REQUEST_URL, Request.Method.GET, new HttpRequestParams() {
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

                    setBalance(data.getDouble("balance"));
                    setTransactions(data.getJSONArray("transactions"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    setLoadingTransactions(false);
                    Utility.toastMessage(WalletActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(WalletActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(WalletActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }

                setLoadingTransactions(false);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();

    }

    public void showTopUpDialog(View view) {

        ArrayList<BottomSheetLayoutModel> layoutModels = new ArrayList<>();

        BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_credit_card, null));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            WalletActivity.this.showTopUpAmountDialog();
        });
        sheetLayoutModel.setText("Credit Card");

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bank_transfer, null));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            Utility.toastMessage(WalletActivity.this, "This feature is coming soon.");
        });
        sheetLayoutModel.setText("Bank Transfer");

        layoutModels.add(sheetLayoutModel);

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, layoutModels);
        bottomSheet.setTitle("Top Up via");
        bottomSheet.show();
    }

    public void showTopUpAmountDialog() {
        View view = getLayoutInflater().inflate(R.layout.topup_amount_bottom_sheet_layout, null);
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, view);
        EditText tvAmount = view.findViewById(R.id.amount);
        Button btnTopUp = view.findViewById(R.id.top_up_btn);
        btnTopUp.setOnClickListener(v -> {
            String amount = tvAmount.getText().toString();
            double mAmount;
            if (amount.isEmpty() || (mAmount = Double.parseDouble(amount)) < 1000) {
                Utility.toastMessage(WalletActivity.this, "Please enter a valid amount");
                return;
            }
            topUpAmount = (float) mAmount;
            bottomSheet.dismiss();
            showTopUpCardsDialog();
        });
        bottomSheet.show();
    }

    public void showTopUpCardsDialog() {
        View view = getLayoutInflater().inflate(R.layout.topup_card_bottom_sheet_layout, null);
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, view);
        LinearLayout cardsContainer = view.findViewById(R.id.cards_container);
        ArrayList<com.quidvis.moneydrop.model.Card> cards = dbHelper.getCards();
        final String[] selectCard = {null};
        for (Card card: cards) {
            View view1 = getCardView(card);
            view1.setOnClickListener(v -> {
                for (int i = 0; i < cardsContainer.getChildCount(); i++) {
                    View view2 = cardsContainer.getChildAt(i);
                    if (view1.getTag().equals(view2.getTag())) {
                        selectCard[0] = (String) view1.getTag();
                        view2.findViewById(R.id.check_mark).setVisibility(View.VISIBLE);
                    } else {
                        view2.findViewById(R.id.check_mark).setVisibility(View.GONE);
                    }
                }
            });
            cardsContainer.addView(view1);
        }
        CircularProgressButton btnTopUp = view.findViewById(R.id.top_up_btn);
        btnTopUp.setOnClickListener(v -> {

            if (topUpAmount < MIN_TOP_UP_AMOUNT) {
                Utility.toastMessage(WalletActivity.this, String.format("Top up amount must be greater than %s.", MIN_TOP_UP_AMOUNT));
                return;
            }

            if (selectCard[0] == null) {
                Utility.toastMessage(WalletActivity.this, "Please select a card");
                return;
            }
            topUp(selectCard[0], btnTopUp, bottomSheet);
        });
        bottomSheet.show();
    }

    private View getCardView(com.quidvis.moneydrop.model.Card card) {

        View cardView = getLayoutInflater().inflate(R.layout.card_layout, null);
        ImageView cardIcon = cardView.findViewById(R.id.card_issuer_icon);
        ImageView selectCheck = cardView.findViewById(R.id.check_mark);
        selectCheck.setVisibility(View.GONE);
        TextView cardNum = cardView.findViewById(R.id.card_number);
        TextView cardExp = cardView.findViewById(R.id.card_expiration);
        TextView cardName = cardView.findViewById(R.id.card_name);

        int icon = CardsActivity.getCardIcon(card.getBrand());
        cardIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), icon, null));
        if (icon == R.drawable.ic_credit_card) {
            int padding = Utility.getDip(this, 5);
            cardIcon.setPadding(padding, padding, padding, padding);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Utility.getDip(this, 10);
        cardView.setLayoutParams(params);
        cardNum.setText(String.format("****  ****  ****  %s", card.getLastFourDigits()));
        cardExp.setText(String.format("%s/%s", card.getExpMonth(), card.getExpYear()));
        String cardBrand = Utility.isEmpty(card.getName(), Utility.ucFirst(card.getBrand()));
        cardBrand = cardBrand.toLowerCase().contains("card") ? cardBrand : String.format("%s card", cardBrand);
        cardName.setText(cardBrand);

        cardView.setTag(card.getUuid());
        return cardView;
    }

    private void topUp(String cardID, CircularProgressButton btnTopUp, CustomBottomSheet bottomSheet) {

        HttpRequest httpRequest = new HttpRequest(this, URLContract.WALLET_TOP_UP_URL + cardID,
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf(topUpAmount));
                return params;
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
                btnTopUp.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                btnTopUp.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {
                        JSONObject data = object.getJSONObject("response");
                        setBalance(data.getDouble("balance"));
                        JSONObject trans = data.getJSONObject("transaction");

                        int size = WalletActivity.data.getJSONArray("transactions").length();
                        WalletActivity.data.getJSONArray("transactions").remove(size - 1);

                        WalletActivity.data.put("transactions", Utility.prependJSONObject(
                                WalletActivity.data.getJSONArray("transactions"), trans));
                        setTransactions(WalletActivity.data.getJSONArray("transactions"));

                        if (MainFragment.data != null) {
                            size = MainFragment.data.getJSONArray("transactions").length();
                            MainFragment.data.getJSONArray("transactions").remove(size - 1);

                            MainFragment.data.put("transactions", Utility.prependJSONObject(
                                    MainFragment.data.getJSONArray("transactions"), trans));

                            MainActivity.saveState(MainFragment.class.getName(), MainFragment.getCurrentState());
                        }
                    }
                    bottomSheet.dismiss();
                    Utility.toastMessage(WalletActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(WalletActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(WalletActivity.this);

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
                    Utility.toastMessage(WalletActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again...");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void showCashoutAmountDialog(View vw) {
        View view = getLayoutInflater().inflate(R.layout.cashout_amount_bottom_sheet_layout, null);
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, view);
        EditText tvAmount = view.findViewById(R.id.amount);
        Button btnCashout = view.findViewById(R.id.cash_out_btn);
        btnCashout.setOnClickListener(v -> {
            String amount = tvAmount.getText().toString();
            double mAmount;
            if (amount.isEmpty() || (mAmount = Double.parseDouble(amount)) < 1000) {
                Utility.toastMessage(WalletActivity.this, "Please enter a valid amount");
                return;
            }
            cashOutAmount = (float) mAmount;
            bottomSheet.dismiss();
            showCashOutAccountsDialog();
        });
        bottomSheet.show();
    }

    public void showCashOutAccountsDialog() {
        View view = getLayoutInflater().inflate(R.layout.cashout_bank_accounts_bottom_sheet_layout, null);
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, view);
        LinearLayout accountsContainer = view.findViewById(R.id.accounts_container);
        ArrayList<BankAccount> accounts = dbHelper.getBankAccounts();
        final String[] selectAccount = {null};
        for (BankAccount account: accounts) {
            View view1 = getAccountView(account);
            view1.setOnClickListener(v -> {
                for (int i = 0; i < accountsContainer.getChildCount(); i++) {
                    View view2 = accountsContainer.getChildAt(i);
                    if (view1.getTag().equals(view2.getTag())) {
                        selectAccount[0] = (String) view1.getTag();
                        view2.findViewById(R.id.check_mark).setVisibility(View.VISIBLE);
                    } else {
                        view2.findViewById(R.id.check_mark).setVisibility(View.GONE);
                    }
                }
            });
            accountsContainer.addView(view1);
        }
        CircularProgressButton btnCashOut = view.findViewById(R.id.cash_out_btn);
        btnCashOut.setOnClickListener(v -> {

            if (cashOutAmount < MIN_TOP_UP_AMOUNT) {
                Utility.toastMessage(WalletActivity.this, String.format("Cash out amount must be greater than %s.", MIN_TOP_UP_AMOUNT));
                return;
            }

            if (selectAccount[0] == null) {
                Utility.toastMessage(WalletActivity.this, "Please select a bank account");
                return;
            }
            cashOut(selectAccount[0], btnCashOut, bottomSheet);
        });
        bottomSheet.show();
    }

    private void cashOut(String recipientCode, CircularProgressButton btnCashout, CustomBottomSheet bottomSheet) {

        HttpRequest httpRequest = new HttpRequest(this, URLContract.WALLET_CASH_OUT_URL + recipientCode,
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf(cashOutAmount));
                return params;
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
                btnCashout.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                btnCashout.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {
                        JSONObject data = object.getJSONObject("response");
                        setBalance(data.getDouble("balance"));
                        JSONObject trans = data.getJSONObject("transaction");

                        int size = WalletActivity.data.getJSONArray("transactions").length();
                        WalletActivity.data.getJSONArray("transactions").remove(size - 1);

                        WalletActivity.data.put("transactions", Utility.prependJSONObject(
                                WalletActivity.data.getJSONArray("transactions"), trans));
                        setTransactions(WalletActivity.data.getJSONArray("transactions"));

                        if (MainFragment.data != null) {
                            size = MainFragment.data.getJSONArray("transactions").length();
                            MainFragment.data.getJSONArray("transactions").remove(size - 1);

                            MainFragment.data.put("transactions", Utility.prependJSONObject(
                                    MainFragment.data.getJSONArray("transactions"), trans));

                            MainActivity.saveState(MainFragment.class.getName(), MainFragment.getCurrentState());
                        }
                    }
                    bottomSheet.dismiss();
                    Utility.toastMessage(WalletActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(WalletActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(WalletActivity.this);

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
                    Utility.toastMessage(WalletActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again...");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private View getAccountView(BankAccount account) {

        View accountView = getLayoutInflater().inflate(R.layout.bank_acount_layout, null);
        ImageView selectCheck = accountView.findViewById(R.id.check_mark);
        TextView acctNum = accountView.findViewById(R.id.account_number);
        TextView acctName = accountView.findViewById(R.id.account_name);
        TextView bankName = accountView.findViewById(R.id.bank_name);

        selectCheck.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Utility.getDip(this, 10);
        accountView.setLayoutParams(params);
        acctNum.setText(account.getAccountNumber());
        acctName.setText(account.getAccountName());
        bankName.setText(Utility.isEmpty(account.getBankName(), "Unknown Bank"));

        accountView.setTag(account.getRecipientCode());
        return accountView;
    }

    public void viewAllTransaction(View view) {
        startActivity(new Intent(this, TransactionsActivity.class));
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }

    public static Bundle getState() {
        return Utility.getState(STATE_KEY);
    }

    public static void saveState(Bundle state) {
        Utility.saveState(STATE_KEY, state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onDestroy() {
        if (savedState != null && data != null) {
            savedState.putString("data", data.toString());
            saveState(savedState);
        }
        super.onDestroy();
    }
}
