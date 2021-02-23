package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.hbb20.CountryCodePicker;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.ProfileActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class ProfileEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private Activity activity;
    private String editOption, editTitle;
    private final ArrayMap<String, Object> editTexts = new ArrayMap<>();
    private TextView tvDobError = null, tvGenderError = null;
    private CircularProgressButton submitBtn;
    private DbHelper dbHelper;
    private User user;
    private final Calendar calendar = Calendar.getInstance();
    private final DateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd", new java.util.Locale("en","ng"));
    private LayoutInflater inflater;
    private ViewGroup container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            editTitle = bundle.getString(ProfileOptionFragment.EDIT_TITLE);
            editOption = bundle.getString(ProfileOptionFragment.EDIT_OPTION);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        return inflater.inflate(R.layout.fragment_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View contentView = null;

        LinearLayout content = view.findViewById(R.id.content);

        TextView title = view.findViewById(R.id.title);
        title.setText(editTitle);

        activity = getActivity();

        dbHelper = new DbHelper(activity);
        user = dbHelper.getUser();

        switch (editOption) {
            case ProfileOptionFragment.EDIT_NAME:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_name, container, false);
                EditText etFirstname = contentView.findViewById(R.id.etFirstname);
                EditText etMiddlename = contentView.findViewById(R.id.etMiddlename);
                EditText etLastname = contentView.findViewById(R.id.etLastname);
                etFirstname.setText(user.getFirstname());
                etMiddlename.setText(Utility.castEmpty(user.getMiddlename()));
                etLastname.setText(user.getLastname());
                editTexts.put("firstname", etFirstname);
                editTexts.put("middlename", etMiddlename);
                editTexts.put("lastname", etLastname);
                break;
            case ProfileOptionFragment.EDIT_PHONE:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_phone, container, false);
                CountryCodePicker ccp = contentView.findViewById(R.id.ccp);
                EditText etPhone = contentView.findViewById(R.id.etPhone);
                ccp.registerCarrierNumberEditText(etPhone);
                ccp.setFullNumber(user.getPhone());
                editTexts.put("phone", ccp);
                break;
            case ProfileOptionFragment.EDIT_EMAIL:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_email, container, false);
                EditText etEmail = contentView.findViewById(R.id.etEmail);
                etEmail.setText(user.getEmail());
                editTexts.put("email", etEmail);
                break;
            case ProfileOptionFragment.EDIT_GENDER:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_gender, container, false);
                LinearLayout maleGender = contentView.findViewById(R.id.male_gender);
                LinearLayout femaleGender = contentView.findViewById(R.id.female_gender);
                ImageView maleChecker = contentView.findViewById(R.id.male_checker);
                ImageView femaleChecker = contentView.findViewById(R.id.female_checker);
                tvGenderError = contentView.findViewById(R.id.tvGenderError);
                maleChecker.setVisibility(user.getGender() == Constant.MALE ? View.VISIBLE : View.GONE);
                femaleChecker.setVisibility(user.getGender() == Constant.FEMALE ? View.VISIBLE : View.GONE);
                EditText etGender = new EditText(activity);
                etGender.setText(String.valueOf(user.getGender()));
                editTexts.put("gender", etGender);
                maleGender.setOnClickListener(v -> {
                    maleChecker.setVisibility(View.VISIBLE);
                    femaleChecker.setVisibility(View.GONE);
                    etGender.setText(String.valueOf(Constant.MALE));
                });
                femaleGender.setOnClickListener(v -> {
                    maleChecker.setVisibility(View.GONE);
                    femaleChecker.setVisibility(View.VISIBLE);
                    etGender.setText(String.valueOf(Constant.FEMALE));
                });
                break;
            case ProfileOptionFragment.EDIT_ADDRESS:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_address, container, false);
                EditText etAddress = contentView.findViewById(R.id.etAddress);
                etAddress.setText(user.getAddress());
                editTexts.put("address", etAddress);
                break;
            case ProfileOptionFragment.EDIT_DOB:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_dob, container, false);
                EditText etDOB = contentView.findViewById(R.id.etDOB);
                tvDobError = contentView.findViewById(R.id.tvDobError);
                etDOB.setText(user.getDob());
                try {
                    calendar.setTime(Objects.requireNonNull(formatter.parse(user.getDob())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                etDOB.setOnClickListener(v -> setDate());
                editTexts.put("dob", etDOB);
                break;
            case ProfileOptionFragment.EDIT_BVN:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_bvn, container, false);
                EditText etBVN = contentView.findViewById(R.id.etBVN);
                etBVN.setText(Utility.castEmpty(user.getBvn()));
                editTexts.put("bvn", etBVN);
                break;
            case ProfileOptionFragment.EDIT_PASSWORD:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_password, container, false);
                EditText etCurrentPassword = contentView.findViewById(R.id.etCurrentPassword);
                EditText etNewPassword = contentView.findViewById(R.id.etNewPassword);
                EditText etConfirmPassword = contentView.findViewById(R.id.etConfirmPassword);
                editTexts.put("current_password", etCurrentPassword);
                editTexts.put("password", etNewPassword);
                editTexts.put("password_confirmation", etConfirmPassword);
                break;
            default:
                break;
        }

        if (contentView != null) {
            submitBtn = contentView.findViewById(R.id.submit);
            submitBtn.setOnClickListener(view1 -> update());
            content.addView(contentView);
        }
    }

    private String getUpdateUriType() {
        String type = null;
        switch (editOption) {
            case ProfileOptionFragment.EDIT_NAME:
                type = "name";
                break;
            case ProfileOptionFragment.EDIT_PHONE:
                type = "phone";
                break;
            case ProfileOptionFragment.EDIT_EMAIL:
                type = "email";
                break;
            case ProfileOptionFragment.EDIT_GENDER:
                type = "gender";
                break;
            case ProfileOptionFragment.EDIT_ADDRESS:
                type = "address";
                break;
            case ProfileOptionFragment.EDIT_DOB:
                type = "dob";
                break;
            case ProfileOptionFragment.EDIT_BVN:
                type = "bvn";
                break;
            case ProfileOptionFragment.EDIT_PASSWORD:
                type = "password";
                break;
        }
        return type;
    }

    private Map<String, String> getUpdateParams() {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, Object> entry : editTexts.entrySet()) {
            String key = entry.getKey();
            Object editText = entry.getValue();
            if (editText instanceof EditText)
                params.put(key, ((EditText) editText).getText().toString());
            else if (editText instanceof CountryCodePicker)
                params.put(key, ((CountryCodePicker) editText).getFullNumberWithPlus());
        }
        return params;
    }

    private void update() {

        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity,
                String.format( URLContract.PROFILE_UPDATE_REQUEST_URL, getUpdateUriType()),
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                return getUpdateParams();
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

                for (Map.Entry<String, Object> entry : editTexts.entrySet()) {
                    EditText editText = getEditText(entry.getValue());
                    if (editText != null) {
                        Utility.disableEditText(editText);
                        Utility.clearFocus(editText, activity);
                    }
                }

                if (tvDobError != null) tvDobError.setVisibility(View.GONE);
                else if (tvGenderError != null) tvGenderError.setVisibility(View.GONE);

                submitBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                submitBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    JSONObject userData = object.getJSONObject("response");

                    if (userData.has("user")) {

                        JSONObject userObject = userData.getJSONObject("user");

                        user.setFirstname(userObject.getString("firstname"));
                        user.setMiddlename(userObject.getString("middlename"));
                        user.setLastname(userObject.getString("lastname"));
                        user.setPhone(userObject.getString("phone"));
                        user.setEmail(userObject.getString("email"));
                        user.setBvn(userObject.getString("bvn"));
                        user.setPicture(userObject.getString("picture"));
                        user.setDob(userObject.getString("dob"));
                        user.setGender(Integer.parseInt(Utility.castNull(userObject.getString("gender"), "0")));
                        user.setAddress(userObject.getString("address"));
                        user.setCountry(userObject.getString("country"));
                        user.setState(userObject.getString("state"));
                        user.setStatus(userObject.getInt("status"));
                        JSONObject verified = userObject.getJSONObject("verified");
                        user.setVerifiedEmail(verified.getBoolean("email"));
                        user.setVerifiedPhone(verified.getBoolean("phone"));

                        if (user.update()) {
                            Utility.toastMessage(activity, object.getString("message"));
                            activity.onBackPressed();
                        } else {
                            Utility.toastMessage(activity, "Failed to update user info locally. Please try again later.");
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    for (Map.Entry<String, Object> entry : editTexts.entrySet()) {
                        EditText editText = getEditText(entry.getValue());
                        if (editText != null) Utility.enableEditText(editText);
                    }
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(activity);

                    dialog.setTitle(object.getString("title"));
                    dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok");
                    dialog.display();

                    JSONObject errors = object.getJSONObject("errors");

                    if (errors.length() > 0) {
                        for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                            String key = it.next();
                            String value = errors.getString(key);
                            if (TextUtils.isEmpty(value)) continue;
                            EditText editText = getEditText(editTexts.get(key));
                            if (tvDobError != null && key.equals("dob")) {
                                tvDobError.setText(value);
                                tvDobError.setVisibility(View.VISIBLE);
                            } else if (tvGenderError != null && key.equals("gender")) {
                                tvGenderError.setText(value);
                                tvGenderError.setVisibility(View.VISIBLE);
                            } else if (editText != null) editText.setError(value);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                for (Map.Entry<String, Object> entry : editTexts.entrySet()) {
                    EditText editText = getEditText(entry.getValue());
                    if (editText != null) Utility.enableEditText(editText);
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void setDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                ProfileEditFragment.this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(year, month, dayOfMonth);
        Objects.requireNonNull(getEditText(editTexts.get("dob"))).setText(formatter.format(calendar.getTime()));
    }

    @Override
    public void onDestroy() {
        clearFocus();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        clearFocus();
        super.onPause();
    }

    private void clearFocus() {
        for (Map.Entry<String, Object> entry : editTexts.entrySet()) {
            EditText editText = getEditText(entry.getValue());
            if (editText != null && editText.hasFocus()) Utility.clearFocus(editText, activity);
        }
    }

    private EditText getEditText(Object object) {
        if (object instanceof CountryCodePicker) object = Utility.getClassField(object, "editText_registeredCarrierNumber");
        if (object instanceof EditText) return ((EditText) object);
        return null;
    }
}