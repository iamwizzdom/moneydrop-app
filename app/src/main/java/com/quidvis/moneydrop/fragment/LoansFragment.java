package com.quidvis.moneydrop.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.adapter.ViewPagerAdapter;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
import com.quidvis.moneydrop.utility.Utility;

import java.util.ArrayList;
import java.util.Objects;

public class LoansFragment extends Fragment {

    private View view;
    private FragmentActivity activity;
    private final ArrayList<CustomFragment> fragments = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.loans_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();

        if (fragments.size() <= 0) {
            LoanOffersFragment loanOffersFragment = LoanOffersFragment.newInstance();
            LoanRequestsFragment loanRequestsFragment = LoanRequestsFragment.newInstance();
            fragments.add(loanOffersFragment);
            fragments.add(loanRequestsFragment);
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter((AppCompatActivity) activity, fragments);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabs = view.findViewById(R.id.tabs);
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
        ((MainActivity) activity).setCustomTitle(activity.getResources().getString(R.string.loan_offers));
        ((MainActivity) activity).setCustomSubtitle(activity.getResources().getString(R.string.no_record));
    }

    private TextView getTextView() {
        TextView tv = new TextView(activity);

        tv.setTextAppearance(activity, R.style.text_view_style);
        tv.setTextColor(activity.getResources().getColor(R.color.colorAccent));

        int padding = Utility.getDip(activity, 10);
        int paddingSides = Utility.getDip(activity, 20);
        tv.setPadding(paddingSides, padding, paddingSides, padding);

        Typeface typeface = ResourcesCompat.getFont(activity, R.font.campton_medium);
        tv.setTypeface(typeface);
        tv.setTextSize(16);

        return tv;
    }

    private void selectView(View view) {
        view.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.layout_background_rounded, null));
        view.setBackgroundTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorWhite)));
    }

    private void deselectView(View view) {
        view.setBackground(null);
        view.setBackgroundTintList(null);
    }

    @Override
    public void onDestroyView() {
        for (CustomFragment fragment : fragments) {
            fragment.saveState();
            fragment.onDestroyView();
        }
        super.onDestroyView();
    }
}