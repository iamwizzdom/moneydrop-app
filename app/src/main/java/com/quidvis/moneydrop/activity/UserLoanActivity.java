package com.quidvis.moneydrop.activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.adapter.ViewPagerAdapter;
import com.quidvis.moneydrop.fragment.LoanOffersFragment;
import com.quidvis.moneydrop.fragment.LoanRequestsFragment;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
import com.quidvis.moneydrop.utility.Utility;

import java.util.ArrayList;
import java.util.Objects;

public class UserLoanActivity extends AppCompatActivity {

    private final static String STATE_KEY = UserLoanActivity.class.getName();
    private final ArrayList<CustomFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_loan);

        if (fragments.size() <= 0) {
            LoanOffersFragment loanOffersFragment = LoanOffersFragment.newInstance();
            LoanRequestsFragment loanRequestsFragment = LoanRequestsFragment.newInstance();
            fragments.add(loanOffersFragment);
            fragments.add(loanRequestsFragment);
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPagerAdapter.setTabLayout(tabs);

        TextView tv1 = getTextView();
        TextView tv2 = getTextView();
        tv1.setText(R.string.offers);
        tv2.setText(R.string.requests);

        Objects.requireNonNull(tabs.getTabAt(0)).setCustomView(tv1);
        Objects.requireNonNull(tabs.getTabAt(1)).setCustomView(tv2);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    View v = tab.getCustomView();
                    if (v != null) selectView(v);
                    viewPagerAdapter.notifyDataSetChanged(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab != null) {
                    View v = tab.getCustomView();
                    if (v != null) deselectView(v);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabs.selectTab(tabs.getTabAt(0), true);
        selectView(Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getCustomView()));
    }

    private TextView getTextView() {
        TextView tv = new TextView(this);

        tv.setTextAppearance(this, R.style.text_view_style);
        tv.setTextColor(this.getResources().getColor(R.color.colorAccent));

        int padding = Utility.getDip(this, 10);
        int paddingSides = Utility.getDip(this, 20);
        tv.setPadding(paddingSides, padding, paddingSides, padding);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.campton_medium);
        tv.setTypeface(typeface);
        tv.setTextSize(16);

        return tv;
    }

    private void selectView(View view) {
        view.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.layout_background_rounded, null));
        view.setBackgroundTintList(ColorStateList.valueOf(this.getResources().getColor(R.color.colorWhite)));
    }

    private void deselectView(View view) {
        view.setBackground(null);
        view.setBackgroundTintList(null);
    }

    public Bundle getState(String key) {
        Bundle state = Utility.getState(STATE_KEY);
        state = state.getBundle(key);
        return state != null ? state : new Bundle();
    }

    public void saveState(String key, Bundle state) {
        Bundle prevState = Utility.getState(STATE_KEY);
        prevState.putBundle(key, state);
        Utility.saveState(STATE_KEY, prevState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}