package com.quidvis.moneydrop.activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.adapter.ViewPagerAdapter;
import com.quidvis.moneydrop.fragment.LoanOffersFragment;
import com.quidvis.moneydrop.fragment.LoanRequestsFragment;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
import com.quidvis.moneydrop.utility.Utility;

import java.util.ArrayList;
import java.util.Objects;

public class UserLoanActivity extends CustomCompatActivity {

    public final static String STATE_KEY = UserLoanActivity.class.getName();
    private final ArrayList<CustomFragment> fragments = new ArrayList<>();
    private TextView itemCount;

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

        itemCount = findViewById(R.id.item_count);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText("OBJECT " + (position + 1))
        ).attach();
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
                    viewPagerAdapter.createFragment(tab.getPosition()).mount();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab != null) {
                    View v = tab.getCustomView();
                    if (v != null) deselectView(v);
                    viewPagerAdapter.createFragment(tab.getPosition()).dismount();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabs.selectTab(tabs.getTabAt(0), true);
        selectView(Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getCustomView()));
        setItemCount(getResources().getString(R.string.no_record));
    }

    private TextView getTextView() {
        TextView tv = new TextView(this);

        tv.setTextAppearance(R.style.text_view_style);
        tv.setTextColor(this.getResources().getColor(R.color.colorAccent, null));

        int padding = Utility.getDip(this, 10);
        int paddingSides = Utility.getDip(this, 20);
        tv.setPadding(paddingSides, padding, paddingSides, padding);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.campton_medium);
        tv.setTypeface(typeface);
        tv.setTextSize(16);

        return tv;
    }

    public void setItemCount(String count) {
        itemCount.setText(count);
    }

    private void selectView(View view) {
        view.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.layout_background_rounded, null));
        view.setBackgroundTintList(ColorStateList.valueOf(this.getResources().getColor(R.color.colorWhite, null)));
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
    protected void onPause() {
        for (CustomFragment fragment : fragments) {
            fragment.saveState();
            fragment.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        for (CustomFragment fragment : fragments) {
            fragment.saveState();
            fragment.onDestroy();
        }
        super.onDestroy();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}