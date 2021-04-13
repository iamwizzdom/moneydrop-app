package com.quidvis.moneydrop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanApplicantsActivity;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;

import java.text.NumberFormat;
import java.util.List;

public class RequestLoanSuccessFragment extends CustomFragment {

    private String amount, message, loanObject;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            amount = bundle.getString("amount");
            message = bundle.getString("message");
            loanObject = bundle.getString("loanObject");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_request_loan_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvAmount = view.findViewById(R.id.amount);
        TextView tvMessage = view.findViewById(R.id.message);
        Button doneBtn = view.findViewById(R.id.done_btn);
        Button requestsBtn = view.findViewById(R.id.view_requests_btn);

        if (amount != null) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
            format.setMaximumFractionDigits(2);
            tvAmount.setText(format.format(Double.valueOf(amount)));
        }
        if (message != null) tvMessage.setText(message);

        doneBtn.setOnClickListener(v ->  {
            Fragment fragment = getParentFragment();
            if (fragment != null) {
                fragment = fragment.getParentFragment();
                if (fragment != null) {
                    fragment = fragment.getParentFragment();
                    if (fragment != null) {
                        List<Fragment> fragments = fragment.getChildFragmentManager().getFragments();
                        for (Fragment frag: fragments) {
                            if (frag instanceof RequestLoanCentralFragment) {
                                ((RequestLoanCentralFragment) frag).loadFragment(v, null, null, null);
                            }
                        }
                    }
                }
            }
        });

        requestsBtn.setOnClickListener(v -> {
            Intent intent  = new Intent(requireActivity(), LoanApplicantsActivity.class);
            intent.putExtra(LoanApplicantsActivity.LOAN_OBJECT, loanObject);
            startActivity(intent);
        });
    }

    @Override
    public void saveState() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void mount() {

    }

    @Override
    public void dismount() {

    }
}