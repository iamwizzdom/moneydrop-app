package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.ProfileActivity;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileOptionFragment extends CustomCompatFragment {

    private User user;
    private Activity activity;

    public static final String EDIT_OPTION = "edit_option";
    public static final String EDIT_TITLE = "edit_title";
    public static final String EDIT_NAME = "edit_name";
    public static final String EDIT_PHONE = "edit_phone";
    public static final String EDIT_EMAIL = "edit_email";
    public static final String EDIT_GENDER = "edit_gender";
    public static final String EDIT_ADDRESS = "edit_address";
    public static final String EDIT_DOB = "edit_dob";
    public static final String EDIT_BVN = "edit_bvn";
    public static final String EDIT_PASSWORD = "edit_password";

    private LinearLayout dobHolder, bvnHolder, emailHolder, phoneHolder, chPassHolder;
    private TextView tvName, tvPhone, tvEmail, tvGender, tvCountry, tvState, tvAddress, tvDob, tvBvn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        activity = getActivity();

        if (bundle != null) {
            String userObject = bundle.getString(ProfileActivity.USER_OBJECT_KEY);
            if (userObject != null) {
                try {
                    user = new User(activity, new JSONObject(userObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dobHolder = view.findViewById(R.id.dob);
        bvnHolder = view.findViewById(R.id.bvn);
        emailHolder = view.findViewById(R.id.email);
        phoneHolder = view.findViewById(R.id.phone_number);
        chPassHolder = view.findViewById(R.id.change_password);

        tvName = view.findViewById(R.id.account_name_text);
        tvPhone = view.findViewById(R.id.phone_number_text);
        tvEmail = view.findViewById(R.id.email_text);
        tvGender = view.findViewById(R.id.gender_text);
        tvCountry = view.findViewById(R.id.country_text);
        tvState = view.findViewById(R.id.state_text);
        tvAddress = view.findViewById(R.id.address_text);
        tvDob = view.findViewById(R.id.dob_text);
        tvBvn = view.findViewById(R.id.bvn_text);

        setUser();
    }

    public void setUser() {
        setUser(this.user);
    }

    public void setUser(User user) {
        if (user == null || (user != null && user.isMe())) {
            DbHelper dbHelper = new DbHelper(activity);
            this.user = user = dbHelper.getUser();
        }

        if (!user.isMe()) {
            dobHolder.setVisibility(View.GONE);
            bvnHolder.setVisibility(View.GONE);
            emailHolder.setVisibility(View.GONE);
            phoneHolder.setVisibility(View.GONE);
            chPassHolder.setVisibility(View.GONE);
        }

        tvName.setText(String.format("%s %s", user.getFirstname(), user.getLastname()));
        tvPhone.setText(user.getPhone());
        tvEmail.setText(user.getEmail());
        tvGender.setText(Utility.convertGender(user.getGender(), "Unknown"));
        tvCountry.setText(Utility.castEmpty(user.getCountry(), "Unknown"));
        tvState.setText(Utility.castEmpty(user.getState(), "Unknown"));
        tvAddress.setText(Utility.castEmpty(user.getAddress(), "Unknown"));
        tvDob.setText(user.getDob());
        tvBvn.setText(Utility.castEmpty(user.getBvn(), "Not set"));
    }
}