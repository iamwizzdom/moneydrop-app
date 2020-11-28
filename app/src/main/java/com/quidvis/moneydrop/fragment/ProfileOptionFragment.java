package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.Utility;

public class ProfileOptionFragment extends Fragment {

    public static final String EDIT_OPTION = "edit_option";
    public static final String EDIT_TITLE = "edit_title";
    public static final String EDIT_NAME = "edit_name";
    public static final String EDIT_PHONE = "edit_phone";
    public static final String EDIT_EMAIL = "edit_email";
    public static final String EDIT_DOB = "edit_dob";
    public static final String EDIT_BVN = "edit_bvn";
    public static final String EDIT_PASSWORD = "edit_password";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvName = view.findViewById(R.id.account_name_text);
        TextView tvPhone = view.findViewById(R.id.phone_number_text);
        TextView tvEmail = view.findViewById(R.id.email_text);
        TextView tvDob = view.findViewById(R.id.dob_text);
        TextView tvBvn = view.findViewById(R.id.bvn_text);

        Activity activity = getActivity();

        DbHelper dbHelper = new DbHelper(activity);

        User user = dbHelper.getUser();
        tvName.setText(String.format("%s %s", user.getFirstname(), user.getLastname()));
        tvPhone.setText(user.getPhone());
        tvEmail.setText(user.getEmail());
        tvDob.setText(user.getDob());
        tvBvn.setText(Utility.isEmpty(user.getBvn(), "Not set"));
    }
}