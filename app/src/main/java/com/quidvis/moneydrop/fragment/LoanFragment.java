package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;

public class LoanFragment extends Fragment {

    private Activity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_loan, container, false);

        activity = getActivity();

        assert activity != null;
        ((MainActivity) activity).setCustomTitle("Offer Loan");
        ((MainActivity) activity).setCustomSubtitle("How much would you like to offer?");

        return root;
    }
}