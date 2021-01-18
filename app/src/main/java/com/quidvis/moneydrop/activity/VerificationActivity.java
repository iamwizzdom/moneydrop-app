package com.quidvis.moneydrop.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.fragment.VerificationFragment;
import com.quidvis.moneydrop.fragment.VerificationOTPFragment;

import java.util.Objects;

public class VerificationActivity extends AppCompatActivity {

    public static String CURRENT_TAG = null;
    public static final String TAG_VERIFICATION = "VERIFICATION", TAG_OTP = "OTP";
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loadFragment(TAG_VERIFICATION);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private Fragment getFragment(String fragmentTag) {

        switch (fragmentTag) {
            case TAG_VERIFICATION:
                // verification
                return VerificationFragment.newInstance();
            case TAG_OTP:
            default:
                return VerificationOTPFragment.newInstance();
        }
    }

    /**
     *
     * @param tag
     */
    public void loadFragment(String tag) {
        loadFragment(tag, null);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    public void loadFragment(final String tag, final Bundle bundle) {
        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect

        Runnable mPendingRunnable = () -> {
            // update the main content by replacing fragments
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment currentFragment = getFragment(CURRENT_TAG = tag);
            if (bundle != null) currentFragment.setArguments(bundle);
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.content_main, currentFragment, tag);
            fragmentTransaction.commitAllowingStateLoss();
        };

        // If mPendingRunnable is not null, then add to the message queue
        mHandler.postDelayed(mPendingRunnable, 300);
    }

    public void onBackPressed(View view) {
        if (Objects.equals(CURRENT_TAG, TAG_VERIFICATION)) onBackPressed();
        else loadFragment(TAG_VERIFICATION);
    }
}
