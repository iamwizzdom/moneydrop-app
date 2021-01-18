package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.interfaces.OnCustomDialogClickListener;
import com.quidvis.moneydrop.model.Bank;
import com.quidvis.moneydrop.model.BankAccount;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.view.DialogSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class BankAccountsActivity extends AppCompatActivity {

    private CircularProgressButton addBankBtn;
    private DbHelper dbHelper;
    private User user;
    private ArrayList<Bank> banks;
    private ArrayList<BankAccount> accounts;

    private LinearLayout accountsContainer, contentView;
    private TextView successfulView, emptyView;
    private CustomBottomSheet bottomSheet;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_accounts);
        dbHelper = new DbHelper(BankAccountsActivity.this);
        user = dbHelper.getUser();
        banks = dbHelper.getBanks();
        accounts = dbHelper.getBankAccounts();

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> this.fetchAllAccounts(true));

        emptyView = findViewById(R.id.empty_view);
        accountsContainer = findViewById(R.id.accounts_container);

        if (!user.getBvn().isEmpty()) {

            if (accounts.size() > 0) listAccounts();
            else fetchAllAccounts();

        } else {

            AwesomeAlertDialog awesomeAlertDialog = new AwesomeAlertDialog(this);
            awesomeAlertDialog.setTitle("BVN Check");
            awesomeAlertDialog.setMessage("Sorry, you must set your BVN before performing any bank operation.");
            awesomeAlertDialog.setPositiveButton("Set BVN", dialog -> {
                startActivity(new Intent(BankAccountsActivity.this, ProfileActivity.class));
            });
            awesomeAlertDialog.setCancelable(false);
            awesomeAlertDialog.setOnCancelListener(dialogInterface -> {
                Utility.toastMessage(BankAccountsActivity.this, "You must set BVN to proceed.");
                finish();
            });
            awesomeAlertDialog.show();
        }
    }

    private void listAccounts() {
        accountsContainer.removeAllViews();
        if (accounts.size() <= 0) {
            isContentEmpty(true);
            return;
        }
        isContentEmpty(false);
        for (BankAccount account: accounts) {
            accountsContainer.addView(getAccountView(account));
        }
    }

    private void isContentEmpty(boolean status) {
        emptyView.setVisibility(status ? View.VISIBLE : View.GONE);
        accountsContainer.setVisibility(status ? View.GONE : View.VISIBLE);
    }

    private View getAccountView(BankAccount account) {

        View accountView = getLayoutInflater().inflate(R.layout.bank_acount_layout, null);
        ImageView optionBtn = accountView.findViewById(R.id.option);
        TextView acctNum = accountView.findViewById(R.id.account_number);
        TextView acctName = accountView.findViewById(R.id.account_name);
        TextView bankName = accountView.findViewById(R.id.bank_name);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Utility.getDip(this, 10);
        accountView.setLayoutParams(params);
        acctNum.setText(account.getAccountNumber());
        acctName.setText(account.getAccountName());
        bankName.setText(Utility.castEmpty(account.getBankName(), "Unknown Bank"));

        optionBtn.setTag(account.getUuid());
        optionBtn.setVisibility(View.VISIBLE);
        optionBtn.setOnClickListener(v -> {

            final PopupMenu popup = new PopupMenu(BankAccountsActivity.this, optionBtn);

            popup.inflate(R.menu.bank_account_option);

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() != R.id.remove) popup.dismiss();
                else {
                    CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(BankAccountsActivity.this);
                    alertDialog.setIcon(R.drawable.ic_remove);
                    alertDialog.setMessage("Are you sure you want to remove this bank account?");
                    alertDialog.setNegativeButton("No, cancel");
                    alertDialog.setPositiveButton("Yes, proceed", vw -> removeBankAccount(accountView, (String) v.getTag()));
                    alertDialog.display();
                }
                return false;
            });

            popup.show();
        });
        return accountView;
    }

    public void addAccount(View view) {

        View bottomSheetView = getLayoutInflater().inflate(R.layout.add_bank_account_bottom_sheet_layout, null);
        contentView = bottomSheetView.findViewById(R.id.content);
        successfulView = bottomSheetView.findViewById(R.id.successful);

        EditText etAcctNum = bottomSheetView.findViewById(R.id.account_number);
        DialogSpinner dialogSpinner = bottomSheetView.findViewById(R.id.banks_list);

        int size = this.banks.size();
        String[] banks = new String[size];
        for (int i = 0; i < size; i++) {
            Bank bank = this.banks.get(i);
            banks[i] = bank.getName();
        }
        dialogSpinner.setEntries(banks);

        addBankBtn = bottomSheetView.findViewById(R.id.add_account);

        addBankBtn.setOnClickListener(v -> {
            String accountNumber = etAcctNum.getText().toString();
            String bankCode = this.banks.get(dialogSpinner.getSelectedItemPosition()).getCode();
            if (accountNumber.isEmpty() || accountNumber.length() < 6) {
                Utility.toastMessage(BankAccountsActivity.this, "Please enter valid account number");
                return;
            }
            if (bankCode.isEmpty()) {
                Utility.toastMessage(BankAccountsActivity.this, "Please select a bank");
                return;
            }
            addBankAccount(accountNumber, bankCode);
        });

        bottomSheet = CustomBottomSheet.newInstance(this, bottomSheetView);
        bottomSheet.show();
    }

    private void addBankAccount(String accountNumber, String bankCode) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.BANK_ACCOUNT_ADD_URL,
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("account_number", accountNumber);
                params.put("bank_code", bankCode);
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
                addBankBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    JSONObject objectResponse = object.getJSONObject("response");

                    if (objectResponse.has("account")) {

                        JSONObject accountObject = objectResponse.getJSONObject("account");

                        BankAccount account = new BankAccount(BankAccountsActivity.this);
                        account.setUuid(accountObject.getString("uuid"));
                        account.setAccountName(accountObject.getString("account_name"));
                        account.setAccountNumber(accountObject.getString("account_number"));
                        account.setBankName(accountObject.getString("bank_name"));
                        account.setRecipientCode(accountObject.getString("recipient_code"));

                        if (dbHelper.saveBankAccount(account)) {
                            accounts.add(0, account);
                            isContentEmpty(false);
                            accountsContainer.addView(getAccountView(account), 0);
                            Utility.toastMessage(BankAccountsActivity.this, object.getString("message"));
                        } else {
                            Utility.alertDialog(BankAccountsActivity.this,
                                    "Sorry we couldn't save this account details locally on " +
                                            "this device at this time. But this will be rectified when next you log in.");
                        }

                    } else {
                        Utility.toastMessage(BankAccountsActivity.this, object.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(BankAccountsActivity.this, "Something unexpected happened. Please try that again.");
                }

                contentView.setVisibility(View.GONE);
                successfulView.setVisibility(View.VISIBLE);
                addBankBtn.revertAnimation();
                addBankBtn.setOnClickListener(v -> bottomSheet.dismiss());
                new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                    addBankBtn.setText(R.string.done);
                }, 500);
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(BankAccountsActivity.this);

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
                    Utility.toastMessage(BankAccountsActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
                addBankBtn.revertAnimation();
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send(Constant.RETRY_IN_60_SEC);
    }

    private void fetchAllAccounts() {
        fetchAllAccounts(false);
    }

    private void fetchAllAccounts(boolean refreshing) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.BANK_ACCOUNT_RETRIEVE_ALL_URL,
                Request.Method.POST, new HttpRequestParams() {
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
                if (refreshing) swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                if (refreshing) swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    JSONObject responseObj = object.getJSONObject("response");
                    JSONArray accountList = responseObj.getJSONArray("accounts");
                    JSONObject banks = responseObj.getJSONObject("banks");

                    int size = accountList.length();

                    if (size > 0) accounts.clear();

                    for (int i = 0; i < size; i++) {
                        JSONObject accountObject = accountList.getJSONObject(i);
                        BankAccount account = new BankAccount(BankAccountsActivity.this);
                        account.setUuid(accountObject.getString("uuid"));
                        account.setAccountName(accountObject.getString("account_name"));
                        account.setAccountNumber(accountObject.getString("account_number"));
                        account.setBankName(accountObject.getString("bank_name"));
                        account.setRecipientCode(accountObject.getString("recipient_code"));
                        accounts.add(account);
                        dbHelper.saveBankAccount(account);
                    }

                    if (banks.length() > 0 && BankAccountsActivity.this.banks.size() > 0)
                        BankAccountsActivity.this.banks.clear();

                    for (Iterator<String> it = banks.keys(); it.hasNext();) {
                        String key = it.next();
                        JSONObject bankObject = banks.getJSONObject(key);
                        Bank bank = new Bank(BankAccountsActivity.this);
                        bank.setUid(bankObject.getInt("id"));
                        bank.setName(bankObject.getString("name"));
                        bank.setCode(bankObject.getString("code"));
                        dbHelper.saveBank(bank);
                        BankAccountsActivity.this.banks.add(bank);
                    }

                    listAccounts();

                    if (refreshing) Utility.toastMessage(BankAccountsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (refreshing) Utility.toastMessage(BankAccountsActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                if (!refreshing) return;
                    try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(BankAccountsActivity.this);

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
                    Utility.toastMessage(BankAccountsActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void removeBankAccount(View view, String accountID) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.BANK_ACCOUNT_REMOVE_URL + accountID,
                Request.Method.GET, new HttpRequestParams() {
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
                Utility.toastMessage(BankAccountsActivity.this, "Removing account");
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {
                        dbHelper.deleteBankAccount(accountID);
                        for (int i = 0; i < accounts.size(); i++) {
                            BankAccount account = accounts.get(i);
                            if (account.getUuid().equals(accountID)) {
                                accounts.remove(i);
                                break;
                            }
                        }
                        accountsContainer.removeView(view);
                        if (accounts.size() <= 0) isContentEmpty(true);
                    }
                    Utility.toastMessage(BankAccountsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(BankAccountsActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(BankAccountsActivity.this);

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
                    Utility.toastMessage(BankAccountsActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}
