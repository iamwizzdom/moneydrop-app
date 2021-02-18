package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;

import java.util.Objects;

public class OfferLoanCentralFragment extends Fragment {

    private Activity activity;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_offer_loan_central, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = requireActivity();
        navController = getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            if (destination.getId() == R.id.nav_offer_loan) {

                ((MainActivity) activity).setCustomTitle("Offer Loan");
                ((MainActivity) activity).setCustomSubtitle("How much would you like to offer?");
            } else {

                ((MainActivity) activity).setCustomTitle("Congrats");
                ((MainActivity) activity).setCustomSubtitle("You have successfully made your offer.");
            }
        });
    }

    public void loadFragment(View view, String amount, String message, String loanObject) {
        Bundle bundle = new Bundle();
        int id = view.getId();
        if (id == R.id.done_btn) {
            navController.popBackStack();
            return;
        }
        bundle.putString("amount", amount);
        bundle.putString("message", message);
        bundle.putString("loanObject", loanObject);
        navController.navigate(R.id.nav_offer_loan_success, bundle, getNavOptions());
    }

    private NavController getNavController() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.nav_offer_loan_fragment);
        if (!(fragment instanceof NavHostFragment)) {
            throw new IllegalStateException("Activity " + this + " does not have a NavHostFragment");
        }
        return ((NavHostFragment) fragment).getNavController();
    }

    protected NavOptions getNavOptions() {

        return new NavOptions.Builder()
                .setEnterAnim(R.anim.from_right)
                .setExitAnim(R.anim.to_left)
                .setPopEnterAnim(R.anim.from_left)
                .setPopExitAnim(R.anim.to_right)
                .build();
    }
}