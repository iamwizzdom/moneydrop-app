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
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.ProfileActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class ProfileEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private Activity activity;
    private String editOption, editTitle;
    private final ArrayMap<String, EditText> editTexts = new ArrayMap<>();
    private TextView tvDobError = null;
    private CircularProgressButton submitBtn;
    private DbHelper dbHelper;
    private ImageView backBtn;
    private final Calendar calendar = Calendar.getInstance();
    private final DateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd", new java.util.Locale("en","ng"));

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
        View contentView = null, view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        LinearLayout content = view.findViewById(R.id.content);

        TextView title = view.findViewById(R.id.title);
        title.setText(editTitle);

        activity = getActivity();

        backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view12 -> ((ProfileActivity) activity).loadEditFragment(view12));

        dbHelper = new DbHelper(activity);

        switch (editOption) {
            case ProfileOptionFragment.EDIT_NAME:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_name, container, false);
                EditText etFirstname = contentView.findViewById(R.id.etFirstname);
                EditText etMiddlename = contentView.findViewById(R.id.etMiddlename);
                EditText etLastname = contentView.findViewById(R.id.etLastname);
                etFirstname.setText(dbHelper.getUser().getFirstname());
                etMiddlename.setText(Utility.isEmpty(dbHelper.getUser().getMiddlename()));
                etLastname.setText(dbHelper.getUser().getLastname());
                editTexts.put("firstname", etFirstname);
                editTexts.put("middlename", etMiddlename);
                editTexts.put("lastname", etLastname);
                break;
            case ProfileOptionFragment.EDIT_PHONE:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_phone, container, false);
                EditText etPhone = contentView.findViewById(R.id.etPhone);
                etPhone.setText(dbHelper.getUser().getPhone());
                editTexts.put("phone", etPhone);
                break;
            case ProfileOptionFragment.EDIT_EMAIL:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_email, container, false);
                EditText etEmail = contentView.findViewById(R.id.etEmail);
                etEmail.setText(dbHelper.getUser().getEmail());
                editTexts.put("email", etEmail);
                break;
            case ProfileOptionFragment.EDIT_DOB:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_dob, container, false);
                EditText etDOB = contentView.findViewById(R.id.etDOB);
                tvDobError = contentView.findViewById(R.id.tvDobError);
                etDOB.setText(dbHelper.getUser().getDob());
                try {
                    calendar.setTime(Objects.requireNonNull(formatter.parse(dbHelper.getUser().getDob())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                etDOB.setOnClickListener(v -> setDate());
                editTexts.put("dob", etDOB);
                break;
            case ProfileOptionFragment.EDIT_BVN:
                contentView = inflater.inflate(R.layout.fragment_profile_edit_bvn, container, false);
                EditText etBVN = contentView.findViewById(R.id.etBVN);
                etBVN.setText(Utility.isEmpty(dbHelper.getUser().getBvn()));
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

        return view;
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

    private Map<String, String> getUpdateData() {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, EditText> entry : editTexts.entrySet()) {
            String key = entry.getKey();
            EditText editText = entry.getValue();
            params.put(key, editText.getText().toString());
        }
        return params;
    }

    private void update() {

        HttpRequest httpRequest = new HttpRequest(String.format("%s/%s", URLContract.PROFILE_UPDATE_REQUEST_URL,
                getUpdateUriType()), Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                return getUpdateData();
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

                for (Map.Entry<String, EditText> entry : editTexts.entrySet()) {
                    EditText editText = entry.getValue();
                    Utility.disableEditText(editText);
                    Utility.clearFocus(editText, activity);
                }

                if (tvDobError != null) tvDobError.setVisibility(View.GONE);
                submitBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    JSONObject userData = object.getJSONObject("response");

                    if (userData.has("user")) {

                        JSONObject userObject = userData.getJSONObject("user");

                        User user = dbHelper.getUser();
                        user.setFirstname(userObject.getString("firstname"));
                        user.setMiddlename(userObject.getString("middlename"));
                        user.setLastname(userObject.getString("lastname"));
                        user.setPhone(userObject.getString("phone"));
                        user.setEmail(userObject.getString("email"));
                        user.setBvn(userObject.getString("bvn"));
                        user.setPicture(userObject.getString("picture"));
                        user.setDob(userObject.getString("dob"));
                        user.setGender(Integer.parseInt(Utility.isNull(userObject.getString("gender"), "0")));
                        user.setAddress(userObject.getString("address"));
                        user.setCountry(userObject.getString("country"));
                        user.setState(userObject.getString("state"));
                        user.setStatus(userObject.getInt("status"));
                        JSONObject verified = userObject.getJSONObject("verified");
                        user.setVerifiedEmail(verified.getBoolean("email"));
                        user.setVerifiedPhone(verified.getBoolean("phone"));

                        if (user.update()) {
                            Utility.toastMessage(activity, object.getString("message"));
                            backBtn.callOnClick();
                        } else {
                            Utility.toastMessage(activity, "Failed to update user info locally. Please try again later.");
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    for (Map.Entry<String, EditText> entry : editTexts.entrySet()) {
                        EditText editText = entry.getValue();
                        Utility.enableEditText(editText);
                    }
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }

                submitBtn.revertAnimation();
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

                    JSONObject errors = object.getJSONObject("error");

                    if (errors.length() > 0) {
                        for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                            String key = it.next();
                            String value = errors.getString(key);
                            if (TextUtils.isEmpty(value)) continue;
                            EditText editText = editTexts.get(key);
                            if (tvDobError != null && key.equals("dob")) {
                                tvDobError.setText(value);
                                tvDobError.setVisibility(View.VISIBLE);
                            } else if (editText != null) editText.setError(value);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                for (Map.Entry<String, EditText> entry : editTexts.entrySet()) {
                    EditText editText = entry.getValue();
                    Utility.enableEditText(editText);
                }
                submitBtn.revertAnimation();
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send(activity);
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
        Objects.requireNonNull(editTexts.get("dob")).setText(formatter.format(calendar.getTime()));
    }
}