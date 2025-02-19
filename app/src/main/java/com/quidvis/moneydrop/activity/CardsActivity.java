package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.flutterwave.raveandroid.rave_presentation.RaveNonUIManager;
import com.flutterwave.raveandroid.rave_presentation.card.CardPaymentCallback;
import com.flutterwave.raveandroid.rave_presentation.card.CardPaymentManager;
import com.flutterwave.raveandroid.rave_presentation.data.AddressDetails;
import com.flutterwave.raveutils.verification.AVSVBVFragment;
import com.flutterwave.raveutils.verification.OTPFragment;
import com.flutterwave.raveutils.verification.PinFragment;
import com.flutterwave.raveutils.verification.RaveVerificationUtils;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.view.EditCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.apachat.loadingbutton.core.customViews.CircularProgressButton;
import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class CardsActivity extends CustomCompatActivity implements CardPaymentCallback {

    private CircularProgressButton addCardBtn;
    private DbHelper dbHelper;
    private User user;
    private ArrayList<com.quidvis.moneydrop.model.Card> cards;
    private String flutterCardName, flutterTransRef;

//    private Animation animFadeIn, animFadeOut;
    private LinearLayout cardsContainer, contentView;
    private TextView successfulView, emptyView;
    private CustomBottomSheet bottomSheet;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RaveNonUIManager raveNonUIManager;
    private RaveVerificationUtils verificationUtils;
    private CardPaymentManager cardPayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        dbHelper = new DbHelper(CardsActivity.this);
        user = dbHelper.getUser();
        cards = dbHelper.getCards();
        PaystackSdk.initialize(this);

//        raveNonUIManager = new RaveNonUIManager();
//        verificationUtils = new RaveVerificationUtils(this, true, Constant.FLUTTERWAVE_PUBKEY, R.style.AppTheme);

//        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
//        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> this.fetchAllCards(true));

        emptyView = findViewById(R.id.empty_view);
        cardsContainer = findViewById(R.id.cards_container);

        if (cards.size() > 0) listCards();
        else fetchAllCards();
    }

    private void listCards() {
        cardsContainer.removeAllViews();
        if (cards.size() <= 0) {
            isContentEmpty(true);
            return;
        }
        isContentEmpty(false);
        for (com.quidvis.moneydrop.model.Card card: cards) {
            cardsContainer.addView(getCardView(card));
        }
    }

    private void isContentEmpty(boolean status) {
        emptyView.setVisibility(status ? View.VISIBLE : View.GONE);
        cardsContainer.setVisibility(status ? View.GONE : View.VISIBLE);
    }

    private View getCardView(com.quidvis.moneydrop.model.Card card) {

        View cardView = getLayoutInflater().inflate(R.layout.card_layout, null);
        ImageView cardIcon = cardView.findViewById(R.id.card_issuer_icon);
        ImageView optionBtn = cardView.findViewById(R.id.option);
        TextView cardNum = cardView.findViewById(R.id.card_number);
        TextView cardExp = cardView.findViewById(R.id.card_expiration);
        TextView cardName = cardView.findViewById(R.id.card_name);

        int icon = getCardIcon(card.getBrand());
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
        cardBrand = cardBrand.toLowerCase().contains("card") ? cardBrand : String.format("%s Card", cardBrand);
        cardName.setText(cardBrand);

        optionBtn.setTag(card.getUuid());
        optionBtn.setVisibility(View.VISIBLE);
        optionBtn.setOnClickListener(v -> {

            final PopupMenu popup = new PopupMenu(CardsActivity.this, optionBtn);

            popup.inflate(R.menu.card_option);

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() != R.id.remove) popup.dismiss();
                else {
                    CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(CardsActivity.this);
                    alertDialog.setIcon(R.drawable.ic_remove);
                    alertDialog.setMessage("Are you sure you want to remove this card?");
                    alertDialog.setNegativeButton("No, cancel");
                    alertDialog.setPositiveButton("Yes, proceed", vw -> removeCard(cardView, (String) v.getTag()));
                    alertDialog.display();
                }
                return false;
            });

            popup.show();
        });
        return cardView;
    }

    public void addCard(View view) {

        bottomSheet = CustomBottomSheet.newInstance(this, R.layout.add_card_bottom_sheet_layout);
        bottomSheet.setOnViewInflatedListener(view1 -> {

            contentView = view1.findViewById(R.id.content);
            successfulView = view1.findViewById(R.id.successful);

            EditCard etCardNum = view1.findViewById(R.id.card_number);
            EditText etCardExp = view1.findViewById(R.id.card_expiration);
            EditText etCardCvv = view1.findViewById(R.id.card_cvv);
            addCardBtn = view1.findViewById(R.id.add_card);
            etCardExp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String initial = s.toString();
                    if (initial.contains("/")) {
                        if (initial.length() < 2) s.replace(0, initial.length(), "0" + initial);
                        return;
                    }
                    // insert a forward slash after all groups of 2 digits that are followed by another digit
                    String processed = initial.replaceAll("(\\d{2})(?=\\d)", "$1/");
                    if (!initial.contentEquals(processed) && processed.length() > 0) {
                        // set the value
                        s.replace(0, initial.length(), processed);
                    }
                }
            });
            InputFilter filter = (source, start, end, dest, dstart, dend) -> {
                String s = String.valueOf(source);
                if (s.startsWith("/")) return "";
                if (s.contains("/")) {
                    String[] sa = s.split("[/]");
                    int num = Integer.parseInt(sa[0]);
                    if (!sa[0].isEmpty() && sa[0].length() <= 2 && (num < 1 || num > 12)) return "";
                }
                return null;
            };
            etCardExp.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(5)});
            etCardCvv.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

            addCardBtn.setOnClickListener(v -> {
                String[] exp = etCardExp.getText().toString().split("[/]");
                if (exp.length < 2 || exp[0].isEmpty() || exp[1].isEmpty()) {
                    Utility.toastMessage(CardsActivity.this, "Please enter valid card details");
                    return;
                }
                int expiryMonth = Integer.parseInt(exp[0]);
                int expiryYear = Integer.parseInt(exp[1]);
                Card card = new Card(etCardNum.getCardNumber(), expiryMonth, expiryYear, etCardCvv.getText().toString());
                if (card.isValid()) {
                    // charge card
                    try {
                        performPaystackCharge(card, etCardNum.getCardType());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utility.toastMessage(CardsActivity.this, "Operation failed.");
                    }
                } else {
                    Utility.toastMessage(CardsActivity.this, "Please enter valid card details");
                }

//                com.flutterwave.raveandroid.rave_presentation.card.Card card = new com.flutterwave.raveandroid.rave_presentation.card.Card(
//                        etCardNum.getCardNumber(), String.valueOf(expiryMonth), String.valueOf(expiryYear), etCardCvv.getText().toString());
//                performFlutterwaveCharge(card, etCardNum.getCardType());

            });
        });
        bottomSheet.show();
    }

//    public void performFlutterwaveCharge(com.flutterwave.raveandroid.rave_presentation.card.Card card, String cardName) {
//        flutterCardName = cardName;
//        raveNonUIManager.setAmount(50)
//                .setCurrency("NGN")
//                .setEmail(user.getEmail())
//                .setfName(user.getFirstname())
//                .setlName(user.getLastname())
//                .setPhoneNumber(user.getPhone())
//                .setNarration("MoneyDrop card test charge")
//                .setPublicKey(Constant.FLUTTERWAVE_PUBKEY)
//                .setEncryptionKey(Constant.FLUTTERWAVE_ENCKEY)
//                .setTxRef(flutterTransRef = UUID.randomUUID().toString())
//                .initialize();
//
//        cardPayManager = new CardPaymentManager(raveNonUIManager, this);
//        cardPayManager.chargeCard(card);
//    }

    public void performPaystackCharge(Card card, String cardName) {
        //create a Charge object
        Charge charge = new Charge();
        charge.setAmount(5000);
        charge.setEmail(user.getEmail());
        charge.setBearer(Charge.Bearer.account);
        charge.setCurrency("NGN");
        charge.setCard(card); //sets the card to charge

        addCardBtn.startAnimation();

        PaystackSdk.chargeCard(CardsActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.
                verifyTransaction(transaction.getReference(), cardName);
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.
                String reference = transaction.getReference();
                if (reference != null && !reference.isEmpty()) logTransRef(reference);
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                //handle error here
                String reference = transaction.getReference();
                if (reference != null && !reference.isEmpty())
                    verifyTransaction(reference, cardName);
                else {
                    addCardBtn.revertAnimation();
                    Utility.toastMessage(CardsActivity.this, error.getMessage());
                }
            }

        });
    }

    private void logTransRef(String reference) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.CARD_TRANS_LOG_URL,
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("reference", reference);
                return params;
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
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void verifyTransaction(String reference, String cardName) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.CARD_VERIFICATION_URL,
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("reference", reference);
                params.put("card_name", cardName);
                return params;
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
                addCardBtn.startAnimation();
                Utility.toastMessage(CardsActivity.this, "Verifying card.");
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                addCardBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    JSONObject objectResponse = object.getJSONObject("response");

                    if (objectResponse.has("card")) {

                        JSONObject cardObject = objectResponse.getJSONObject("card");

                        com.quidvis.moneydrop.model.Card card = new com.quidvis.moneydrop.model.Card(CardsActivity.this);
                        card.setUuid(cardObject.getString("uuid"));
                        card.setName(cardObject.getString("name"));
                        card.setLastFourDigits(cardObject.getString("last4digits"));
                        card.setBrand(cardObject.getString("brand"));
                        card.setExpMonth(cardObject.getString("exp_month"));
                        card.setExpYear(cardObject.getString("exp_year"));

                        if (dbHelper.saveCard(card)) {
                            cards.add(0, card);
                            isContentEmpty(false);
                            cardsContainer.addView(getCardView(card), 0);
                            Utility.toastMessage(CardsActivity.this, object.getString("message"));
                        } else {
                            Utility.alertDialog(CardsActivity.this,
                                    "Sorry we couldn't save this card details locally on " +
                                            "this device at this time. But this will be rectified when next you log in.");
                        }

                    } else {
                        Utility.toastMessage(CardsActivity.this, object.getString("message"));
                    }

                    contentView.setVisibility(View.GONE);
                    successfulView.setVisibility(View.VISIBLE);
                    addCardBtn.setOnClickListener(v -> bottomSheet.dismiss());
                    new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> addCardBtn.setText(R.string.done), 500);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(CardsActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(CardsActivity.this);

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
                    Utility.toastMessage(CardsActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
                addCardBtn.revertAnimation();
                logTransRef(flutterTransRef);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void fetchAllCards() {
        fetchAllCards(false);
    }

    private void fetchAllCards(boolean refreshing) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.CARD_RETRIEVE_ALL_URL,
                Request.Method.GET, new HttpRequestParams() {
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
                    JSONArray cardList = object.getJSONArray("response");

                    int size = cardList.length();

                    if (refreshing) dbHelper.deleteAllCards();
                    cards.clear();

                    for (int i = 0; i < size; i++) {
                        JSONObject cardObject = cardList.getJSONObject(i);
                        com.quidvis.moneydrop.model.Card card = new com.quidvis.moneydrop.model.Card(CardsActivity.this);
                        card.setUuid(cardObject.getString("uuid"));
                        card.setName(cardObject.getString("name"));
                        card.setLastFourDigits(cardObject.getString("last4digits"));
                        card.setBrand(cardObject.getString("brand"));
                        card.setExpMonth(cardObject.getString("exp_month"));
                        card.setExpYear(cardObject.getString("exp_year"));
                        cards.add(card);
                        dbHelper.saveCard(card);
                    }

                    listCards();
                    if (refreshing) Utility.toastMessage(CardsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (refreshing) Utility.toastMessage(CardsActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                if (!refreshing) return;
                    try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(CardsActivity.this);

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
                    Utility.toastMessage(CardsActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void removeCard(View view, String cardID) {
        HttpRequest httpRequest = new HttpRequest(this, URLContract.CARD_REMOVE_URL + cardID,
                Request.Method.GET, new HttpRequestParams() {
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
                Utility.toastMessage(CardsActivity.this, "Removing card");
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {
                        dbHelper.deleteCard(cardID);
                        for (int i = 0; i < cards.size(); i++) {
                            com.quidvis.moneydrop.model.Card card = cards.get(i);
                            if (card.getUuid().equals(cardID)) {
                                cards.remove(i);
                                break;
                            }
                        }
                        cardsContainer.removeView(view);
                        if (cards.size() <= 0) isContentEmpty(true);
                    }
                    Utility.toastMessage(CardsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(CardsActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(CardsActivity.this);

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
                    Utility.toastMessage(CardsActivity.this, statusCode == 503 ? error :
                            e.getMessage() + ": Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public static int getCardIcon(String brand) {
        brand = brand.toLowerCase();
        if (brand.contains("visa")) return R.drawable.ic_visa_card;
        else if (brand.contains("verve")) return R.drawable.ic_verve;
        else if (brand.contains("master")) return R.drawable.ic_master_card;
        else if (brand.contains("maestro")) return R.drawable.ic_maestro;
        else if (brand.contains("express")) return R.drawable.ic_american_express_card;
        else if (brand.contains("discover")) return R.drawable.ic_discover_card;
        else if (brand.contains("diners")) return R.drawable.ic_diners_club_card;
        else if (brand.contains("jcb")) return R.drawable.ic_jcb_card;
        return R.drawable.ic_credit_card;
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }

    @Override
    public void showProgressIndicator(boolean active) {
        if (addCardBtn == null) return;
        if (active) addCardBtn.startAnimation();
        else addCardBtn.revertAnimation();
    }

    @Override
    public void collectCardPin() {
        verificationUtils.showPinScreen();
    }

    @Override
    public void collectOtp(String message) {
        verificationUtils.showOtpScreen(message);
    }

    @Override
    public void collectAddress() {
        verificationUtils.showAddressScreen();
    }

    @Override
    public void showAuthenticationWebPage(String authenticationUrl) {
        verificationUtils.showWebpageVerificationScreen(authenticationUrl);
    }

    @Override
    public void onError(String errorMessage, @Nullable String flwRef) {
        if (flutterTransRef != null && !flutterTransRef.isEmpty()) logTransRef(flutterTransRef);
        Utility.toastMessage(CardsActivity.this, errorMessage);
    }

    @Override
    public void onSuccessful(String flwRef) {
        new Handler().postDelayed(() -> {
            if (flutterTransRef != null && !flutterTransRef.isEmpty()) verifyTransaction(flutterTransRef, flutterCardName);
            else Utility.toastMessage(CardsActivity.this, "No transaction reference to verify");
        }, 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RaveConstants.RESULT_SUCCESS) {
            switch (requestCode) {
                case RaveConstants.PIN_REQUEST_CODE:
                    assert data != null;
                    String pin = data.getStringExtra(PinFragment.EXTRA_PIN);
                    // Use the collected PIN
                    cardPayManager.submitPin(pin);
                    break;
                case RaveConstants.ADDRESS_DETAILS_REQUEST_CODE:
                    assert data != null;
                    String streetAddress = data.getStringExtra(AVSVBVFragment.EXTRA_ADDRESS);
                    String state = data.getStringExtra(AVSVBVFragment.EXTRA_STATE);
                    String city = data.getStringExtra(AVSVBVFragment.EXTRA_CITY);
                    String zipCode = data.getStringExtra(AVSVBVFragment.EXTRA_ZIPCODE);
                    String country = data.getStringExtra(AVSVBVFragment.EXTRA_COUNTRY);
                    AddressDetails address = new AddressDetails(streetAddress, city, state, zipCode, country);

                    // Use the address details
                    cardPayManager.submitAddress(address);
                    break;
                case RaveConstants.WEB_VERIFICATION_REQUEST_CODE:
                    // Web authentication complete, proceed
                    cardPayManager.onWebpageAuthenticationComplete();
                    break;
                case RaveConstants.OTP_REQUEST_CODE:
                    assert data != null;
                    String otp = data.getStringExtra(OTPFragment.EXTRA_OTP);
                    // Use OTP
                    cardPayManager.submitOtp(otp);
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
