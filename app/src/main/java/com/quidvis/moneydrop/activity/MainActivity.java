package com.quidvis.moneydrop.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.MainFragment;
import com.quidvis.moneydrop.fragment.WalletFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.BankAccount;
import com.quidvis.moneydrop.model.Card;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.NotificationUtils;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.apachat.loadingbutton.core.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends CustomCompatActivity {

    public final static String START_FRAGMENT = "startFragment";
    public final static String STATE_KEY = MainActivity.class.getName();
    private LinearLayout backBtnHolder, titleHolder;
    private TextView titleTv, subtitleTv;
    private BottomNavigationView navView;
    private NavController navController;
    private DbHelper dbHelper;
    private float topUpAmount = 0, cashOutAmount = 0;
    private final int MIN_TOP_UP_AMOUNT = 1000;

    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));

    private CircleImageView profilePic;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        try {
            loadFragment(item.getItemId());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleHolder = findViewById(R.id.titleHolder);
        backBtnHolder = findViewById(R.id.backBtnHolder);
        profilePic = findViewById(R.id.profile_pic);
        titleTv = findViewById(R.id.title);
        subtitleTv = findViewById(R.id.subtitle);

        dbHelper = new DbHelper(this);
        format.setMaximumFractionDigits(2);

        setUserPic();

        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_loan).build();
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController = getNavController();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navView.setSelectedItemId(Objects.requireNonNull(navController.getCurrentDestination()).getId());
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        NavigationUI.setupWithNavController(navView, navController);

        Intent intent = getIntent();

        if (intent != null) {
            String fragment = intent.getStringExtra(START_FRAGMENT);
            if (fragment != null) {
                int id = getFragmentID(fragment);
                if (id != 0) loadFragment(id);
            }
        }

    }

    private int getFragmentID(String fragment) {
        switch (fragment) {
            case "wallet":
                return R.id.nav_wallet;
            case "history":
                return R.id.nav_history;
        }
        return 0;
    }

    public void loadFragment(View view) {
        loadFragment(view.getId());
    }

    public void loadFragment(int id) {
        if (id == R.id.nav_history || id == R.id.nav_offer_loan || id == R.id.nav_request_loan) {
            backBtnHolder.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleHolder.getLayoutParams();
            params.weight = 1.9f;
            titleHolder.setLayoutParams(params);
        } else {
            backBtnHolder.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleHolder.getLayoutParams();
            params.weight = 2.2f;
            titleHolder.setLayoutParams(params);
        }
        navController.navigate(id, null, getNavOptions());
    }

    @Override
    public void onBackPressed() {
        if (navController.popBackStack()) {
            navView.setSelectedItemId(Objects.requireNonNull(navController.getCurrentDestination()).getId());
        }
        super.onBackPressed();
    }

    private void setUserPic() {

        User user = dbHelper.getUser();

        Glide.with(MainActivity.this)
                .load(user.getPicture())
                .placeholder(user.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(user.getDefaultPicture())
                .into(profilePic);

        profilePic.setOnClickListener(view -> showBottomSheetDialog());
    }

    public void setCustomTitle(String title) {
        titleTv.setText(title);
    }

    public void setCustomSubtitle(String title) {
        subtitleTv.setText(title);
    }

    private NavController getNavController() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (!(fragment instanceof NavHostFragment)) {
            throw new IllegalStateException("Activity " + this + " does not have a NavHostFragment");
        }
        return ((NavHostFragment) fragment).getNavController();
    }

    protected NavOptions getNavOptions() {

        return new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_up)
                .setExitAnim(R.anim.slide_down)
                .setPopEnterAnim(R.anim.slide_up)
                .setPopExitAnim(R.anim.slide_down)
                .build();
    }

    public static void logout(Activity activity) {
        logout(activity, null);
    }

    public static void logout(Activity activity, String title, String message, int code) {
        logout(activity, title, message, code, null, null);
    }

    public static void logout(Activity activity, String message) {
        logout(activity, null, message, 0, null, null);
    }

    public static void logout(Activity activity, String message, Session session) {
        logout(activity, null, message, 0, null, session);
    }

    public static void logout(Activity activity, String message, DbHelper dbHelper) {
        logout(activity, null, message, 0, dbHelper, null);
    }

    public static void logout(Activity activity, String title, String message, int code, DbHelper dbHelper, Session session) {

        if (dbHelper == null) dbHelper = new DbHelper(activity);
        if (session == null) session = new Session(activity);
        Session finalSession = session;
        DbHelper finalDbHelper = dbHelper;

        CustomBottomAlertDialog dialog = new CustomBottomAlertDialog((AppCompatActivity) activity);
        if (code == 419) {
            dialog.setLottieIcon(R.raw.locked);
            message = "Current session expired, please login again to continue.";
        } else dialog.setIcon(R.drawable.ic_log_me_out);
        dialog.setMessage(message != null ? message : "Are you sure you want to logout?");
        dialog.setNegativeButton("No, Cancel");
        String finalMessage = code != 419 ? message : null;
        dialog.setPositiveButton(String.format("%s, Log me out", code == 419 ? "Ok" : "Yes"), v -> {

            Utility.toastMessage(activity, "Logging out, please wait.");

            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {

                finalDbHelper.deleteUser();
                finalDbHelper.deleteAllCards();
                finalDbHelper.deleteAllBankAccounts();
                finalSession.clearAll();

                Intent intent = new Intent(activity, LoginActivity.class);
                if (title != null) intent.putExtra(LoginActivity.TITLE, title);
                if (finalMessage != null) intent.putExtra(LoginActivity.MESSAGE, finalMessage);
                activity.startActivity(intent);
                activity.finish();

            }).addOnFailureListener(e -> Utility.toastMessage(activity, "Logout failed, please try again."));

        });
        dialog.display();
    }

    public void viewAllLoanRequest(View view) {
        startActivity(new Intent(this, UserLoanActivity.class));
    }

    public void viewAllTransaction(View view) {
        startActivity(new Intent(this, TransactionsActivity.class));
    }

    public void showBottomSheetDialog() {

        ArrayList<BottomSheetLayoutModel> layoutModels = new ArrayList<>();

        BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_user, this);
        sheetLayoutModel.setText(getResources().getString(R.string.user_account));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_loan, this);
        sheetLayoutModel.setText(getResources().getString(R.string.my_loans));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            startActivity(new Intent(MainActivity.this, UserLoanActivity.class));
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_transactions, this);
        sheetLayoutModel.setText(getResources().getString(R.string.transactions));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            viewAllTransaction(v);
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_bank, this);
        sheetLayoutModel.setText(getResources().getString(R.string.bank_accounts));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            startActivity(new Intent(MainActivity.this, BankAccountsActivity.class));
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_credit_card, this);
        sheetLayoutModel.setText(getResources().getString(R.string.cards));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            startActivity(new Intent(MainActivity.this, CardsActivity.class));
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_logout, this);
        sheetLayoutModel.setText(getResources().getString(R.string.logout));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            logout(this);
        });

        layoutModels.add(sheetLayoutModel);

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, layoutModels);
        bottomSheet.show();
    }

    public void showTopUpDialog(View view) {

        ArrayList<BottomSheetLayoutModel> layoutModels = new ArrayList<>();

        BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_credit_card, this);
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            MainActivity.this.showTopUpAmountDialog();
        });
        sheetLayoutModel.setText("Credit Card");

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_bank_transfer, this);
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            Utility.toastMessage(MainActivity.this, "This feature is coming soon.");
        });
        sheetLayoutModel.setText("Bank Transfer");

        layoutModels.add(sheetLayoutModel);

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, layoutModels);
        bottomSheet.setTitle("Top Up via");
        bottomSheet.show();
    }

    public void showTopUpAmountDialog() {
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, R.layout.enter_amount_bottom_sheet_layout);
        bottomSheet.setOnViewInflatedListener(view -> {
            EditText tvAmount = view.findViewById(R.id.amount);
            CircularProgressButton submitBtn = view.findViewById(R.id.submit_btn);
            submitBtn.setOnClickListener(v -> {
                String amount = tvAmount.getText().toString();
                double dAmount;
                if (amount.isEmpty() || (dAmount = Double.parseDouble(amount)) < 1000) {
                    Utility.toastMessage(MainActivity.this, "Please enter a valid amount");
                    return;
                }
                topUpAmount = (float) dAmount;
                bottomSheet.dismiss();
                showTopUpCardsDialog();
            });
        });
        bottomSheet.show();
    }

    public void showTopUpCardsDialog() {
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, R.layout.topup_card_bottom_sheet_layout);
        bottomSheet.setOnViewInflatedListener(view -> {
            LinearLayout cardsContainer = view.findViewById(R.id.cards_container);
            TextView cardsEmpty = view.findViewById(R.id.cards_empty);
            CircularProgressButton submitBtn = view.findViewById(R.id.submit_btn);

            ArrayList<Card> cards = dbHelper.getCards();
            if (cards.size() > 0) {

                cardsEmpty.setVisibility(View.GONE);
                cardsContainer.setVisibility(View.VISIBLE);

                final String[] selectCard = {null};

                for (Card card: cards) {
                    View cardView = getCardView(card);
                    cardView.setOnClickListener(v -> {
                        for (int i = 0; i < cardsContainer.getChildCount(); i++) {
                            View view2 = cardsContainer.getChildAt(i);
                            if (cardView.getTag().equals(view2.getTag())) {
                                selectCard[0] = (String) cardView.getTag();
                                view2.findViewById(R.id.check_mark).setVisibility(View.VISIBLE);
                            } else {
                                view2.findViewById(R.id.check_mark).setVisibility(View.GONE);
                            }
                        }
                    });
                    cardsContainer.addView(cardView);
                }

                submitBtn.setOnClickListener(v -> {

                    if (topUpAmount < MIN_TOP_UP_AMOUNT) {
                        Utility.toastMessage(MainActivity.this, String.format("Top up amount must be greater than %s.", MIN_TOP_UP_AMOUNT));
                        return;
                    }

                    if (selectCard[0] == null) {
                        Utility.toastMessage(MainActivity.this, "Please select a card");
                        return;
                    }
                    topUp(selectCard[0], submitBtn, bottomSheet);
                });

            } else {
                cardsEmpty.setVisibility(View.VISIBLE);
                cardsContainer.setVisibility(View.GONE);
                submitBtn.setText(R.string.add_card);
                submitBtn.setOnClickListener(v -> {
                    bottomSheet.dismiss();
                    startActivity(new Intent(MainActivity.this, CardsActivity.class));
                });
            }
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
        String cardBrand = Utility.castEmpty(card.getName(), Utility.ucFirst(card.getBrand()));
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

                        JSONObject responseObject = object.getJSONObject("response");

                        double balance = responseObject.getDouble("balance");
                        double availableBalance = responseObject.getDouble("available_balance");
                        JSONObject transaction = responseObject.getJSONObject("transaction");

                        int size; JSONArray transactions;

                        MainFragment mainFragment = null;
                        WalletFragment walletFragment = null;

                        List<Fragment> fragments = getSupportFragmentManager().getFragments();
                        for (Fragment fragment: fragments) {
                            if (fragment instanceof NavHostFragment) {
                                fragments = fragment.getChildFragmentManager().getFragments();
                                for (Fragment frag: fragments) {
                                    if (frag instanceof MainFragment) mainFragment = (MainFragment) frag;
                                    else if (frag instanceof WalletFragment) walletFragment = (WalletFragment) frag;
                                }
                            }
                        }

                        if (mainFragment != null && walletFragment != null) {

                            JSONObject mainFragmentData = mainFragment.getData();

                            if (mainFragmentData != null) {

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("available_balance", availableBalance);
                                mainFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                mainFragment.setTransactions(transactions);
                                mainFragment.saveState();
                                mainFragment.setBalance(availableBalance);
                            }

                            JSONObject walletFragmentData = walletFragment.getData();

                            if (walletFragmentData != null) {

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("available_balance", availableBalance);
                                walletFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                walletFragment.setTransactions(transactions);
                                walletFragment.saveState();
                                walletFragment.setBalance(balance, availableBalance);
                            }

                        } else if (mainFragment != null) {

                            JSONObject mainFragmentData = mainFragment.getData();

                            if (mainFragmentData != null) {

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("available_balance", availableBalance);
                                mainFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                mainFragment.setTransactions(transactions);
                                mainFragment.saveState();
                                mainFragment.setBalance(availableBalance);
                            }

                            Bundle walletFragmentState = Utility.getState(WalletFragment.STATE_KEY);
                            String walletFragmentStringData = walletFragmentState.getString("data");

                            if (walletFragmentStringData != null) {

                                JSONObject walletFragmentData = new JSONObject(walletFragmentStringData);

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 4) transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("available_balance", availableBalance);
                                walletFragmentData.put("transactions", Utility.prependJSONObject(transactions, transaction));

                                walletFragmentState.putString("data", walletFragmentData.toString());
                                Utility.saveState(WalletFragment.STATE_KEY, walletFragmentState);
                            }

                        } else if (walletFragment != null) {

                            JSONObject walletFragmentData = walletFragment.getData();

                            if (walletFragmentData != null) {

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 4) transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("available_balance", availableBalance);
                                walletFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                walletFragment.setTransactions(transactions);
                                walletFragment.saveState();
                                walletFragment.setBalance(balance, availableBalance);
                            }

                            Bundle mainFragmentState = Utility.getState(MainFragment.STATE_KEY);
                            String mainFragmentStringData = mainFragmentState.getString("data");

                            if (mainFragmentStringData != null) {

                                JSONObject mainFragmentData = new JSONObject(mainFragmentStringData);

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("available_balance", availableBalance);
                                mainFragmentData.put("transactions", Utility.prependJSONObject(transactions, transaction));

                                mainFragmentState.putString("data", mainFragmentData.toString());
                                Utility.saveState(MainFragment.STATE_KEY, mainFragmentState);
                            }

                        }
                    }

                    bottomSheet.dismiss();
                    Utility.toastMessage(MainActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(MainActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(MainActivity.this);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("errors") && object.getJSONObject("errors").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("errors"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", Dialog::dismiss);

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(MainActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void showCashoutAmountDialog(View vw) {
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, R.layout.cashout_amount_bottom_sheet_layout);
        bottomSheet.setOnViewInflatedListener(view -> {
            EditText tvAmount = view.findViewById(R.id.amount);
            Button btnCashout = view.findViewById(R.id.cash_out_btn);
            btnCashout.setOnClickListener(v -> {
                String amount = tvAmount.getText().toString();
                double mAmount;
                if (amount.isEmpty() || (mAmount = Double.parseDouble(amount)) < 1000) {
                    Utility.toastMessage(MainActivity.this, "Please enter a valid amount");
                    return;
                }
                cashOutAmount = (float) mAmount;
                bottomSheet.dismiss();
                showCashOutAccountsDialog();
            });
        });
        bottomSheet.show();
    }

    public void showCashOutAccountsDialog() {
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, R.layout.cashout_bank_accounts_bottom_sheet_layout);
        bottomSheet.setOnViewInflatedListener(view -> {
            LinearLayout accountsContainer = view.findViewById(R.id.accounts_container);
            TextView accountsEmpty = view.findViewById(R.id.accounts_empty);
            CircularProgressButton btnCashOut = view.findViewById(R.id.cash_out_btn);

            ArrayList<BankAccount> accounts = dbHelper.getBankAccounts();

            if (accounts.size() > 0) {

                accountsEmpty.setVisibility(View.GONE);
                accountsContainer.setVisibility(View.VISIBLE);

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

                btnCashOut.setOnClickListener(v -> {

                    if (cashOutAmount < MIN_TOP_UP_AMOUNT) {
                        Utility.toastMessage(MainActivity.this, String.format("Cash out amount must be greater than %s.", MIN_TOP_UP_AMOUNT));
                        return;
                    }

                    if (selectAccount[0] == null) {
                        Utility.toastMessage(MainActivity.this, "Please select a bank account");
                        return;
                    }
                    cashOut(selectAccount[0], btnCashOut, bottomSheet);
                });

            } else {

                accountsEmpty.setVisibility(View.VISIBLE);
                accountsContainer.setVisibility(View.GONE);
                btnCashOut.setText(R.string.add_account);
                btnCashOut.setOnClickListener(v -> {
                    bottomSheet.dismiss();
                    startActivity(new Intent(MainActivity.this, BankAccountsActivity.class));
                });
            }
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

                        JSONObject responseObject = object.getJSONObject("response");

                        double balance = responseObject.getDouble("balance");
                        double availableBalance = responseObject.getDouble("available_balance");
                        JSONObject transaction = responseObject.getJSONObject("transaction");

                        int size; JSONArray transactions;

                        MainFragment mainFragment = null;
                        WalletFragment walletFragment = null;

                        List<Fragment> fragments = getSupportFragmentManager().getFragments();
                        for (Fragment fragment: fragments) {
                            if (fragment instanceof NavHostFragment) {
                                fragments = fragment.getChildFragmentManager().getFragments();
                                for (Fragment frag: fragments) {
                                    if (frag instanceof MainFragment) mainFragment = (MainFragment) frag;
                                    else if (frag instanceof WalletFragment) walletFragment = (WalletFragment) frag;
                                }
                            }
                        }

                        if (mainFragment != null && walletFragment != null) {

                            JSONObject mainFragmentData = mainFragment.getData();

                            if (mainFragmentData != null) {

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("available_balance", availableBalance);
                                mainFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                mainFragment.setTransactions(transactions);
                                mainFragment.saveState();
                                mainFragment.setBalance(balance);
                            }

                            JSONObject walletFragmentData = walletFragment.getData();

                            if (walletFragmentData != null) {

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("available_balance", availableBalance);
                                walletFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                walletFragment.setTransactions(transactions);
                                walletFragment.saveState();
                                walletFragment.setBalance(balance, availableBalance);
                            }

                        } else if (mainFragment != null) {

                            JSONObject mainFragmentData = mainFragment.getData();

                            if (mainFragmentData != null) {

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("available_balance", availableBalance);
                                mainFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                mainFragment.setTransactions(transactions);
                                mainFragment.saveState();
                                mainFragment.setBalance(balance);
                            }

                            Bundle walletFragmentState = Utility.getState(WalletFragment.STATE_KEY);
                            String walletFragmentStringData = walletFragmentState.getString("data");

                            if (walletFragmentStringData != null) {

                                JSONObject walletFragmentData = new JSONObject(walletFragmentStringData);

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("available_balance", availableBalance);
                                walletFragmentData.put("transactions", Utility.prependJSONObject(transactions, transaction));

                                walletFragmentState.putString("data", walletFragmentData.toString());
                                Utility.saveState(WalletFragment.STATE_KEY, walletFragmentState);
                            }

                        } else if (walletFragment != null) {

                            JSONObject walletFragmentData = walletFragment.getData();

                            if (walletFragmentData != null) {

                                transactions = walletFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                walletFragmentData.put("balance", balance);
                                walletFragmentData.put("available_balance", availableBalance);
                                walletFragmentData.put("transactions", transactions = Utility.prependJSONObject(transactions, transaction));
                                walletFragment.setTransactions(transactions);
                                walletFragment.saveState();
                                walletFragment.setBalance(balance, availableBalance);
                            }

                            Bundle mainFragmentState = Utility.getState(MainFragment.STATE_KEY);
                            String mainFragmentStringData = mainFragmentState.getString("data");

                            if (mainFragmentStringData != null) {

                                JSONObject mainFragmentData = new JSONObject(mainFragmentStringData);

                                transactions = mainFragmentData.getJSONArray("transactions");
                                size = transactions.length();
                                if (size > 1) transactions.remove(size - 1);

                                mainFragmentData.put("balance", balance);
                                mainFragmentData.put("available_balance", availableBalance);
                                mainFragmentData.put("transactions", Utility.prependJSONObject(transactions, transaction));

                                mainFragmentState.putString("data", mainFragmentData.toString());
                                Utility.saveState(MainFragment.STATE_KEY, mainFragmentState);
                            }

                        }

                    }
                    bottomSheet.dismiss();
                    Utility.toastMessage(MainActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(MainActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(MainActivity.this);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("errors") && object.getJSONObject("errors").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("errors"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", Dialog::dismiss);

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(MainActivity.this, statusCode == 503 ? error :
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
        LinearLayout container = accountView.findViewById(R.id.container);
        ShimmerFrameLayout shimmerFrameLayout = accountView.findViewById(R.id.bank_shimmer_view);
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
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
        bankName.setText(Utility.castEmpty(account.getBankName(), "Unknown Bank"));

        accountView.setTag(account.getUuid());
        return accountView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!(new Session(this)).isLoggedIn()) {
            Utility.clearAllState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());

        setUserPic();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}