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
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileOptionFragment extends Fragment {

    private User user;
    private Activity activity;

    public static final String EDIT_OPTION = "edit_option";
    public static final String EDIT_TITLE = "edit_title";
    public static final String EDIT_NAME = "edit_name";
    public static final String EDIT_PHONE = "edit_phone";
    public static final String EDIT_EMAIL = "edit_email";
    public static final String EDIT_DOB = "edit_dob";
    public static final String EDIT_BVN = "edit_bvn";
    public static final String EDIT_PASSWORD = "edit_password";

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

        LinearLayout dobHolder = view.findViewById(R.id.dob);
        LinearLayout bvnHolder = view.findViewById(R.id.bvn);
        LinearLayout emailHolder = view.findViewById(R.id.email);
        LinearLayout phoneHolder = view.findViewById(R.id.phone_number);
        LinearLayout chPassHolder = view.findViewById(R.id.change_password);

        TextView tvName = view.findViewById(R.id.account_name_text);
        TextView tvPhone = view.findViewById(R.id.phone_number_text);
        TextView tvEmail = view.findViewById(R.id.email_text);
        TextView tvGender = view.findViewById(R.id.gender_text);
        TextView tvCountry = view.findViewById(R.id.country_text);
        TextView tvState = view.findViewById(R.id.state_text);
        TextView tvAddress = view.findViewById(R.id.address_text);
        TextView tvDob = view.findViewById(R.id.dob_text);
        TextView tvBvn = view.findViewById(R.id.bvn_text);

        if (user == null) {
            DbHelper dbHelper = new DbHelper(activity);
            user = dbHelper.getUser();
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