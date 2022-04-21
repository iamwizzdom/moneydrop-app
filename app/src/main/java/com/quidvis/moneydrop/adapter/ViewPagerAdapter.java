package com.quidvis.moneydrop.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.UserLoanActivity;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private final ArrayList<CustomFragment> mFragments;
    private final AppCompatActivity mActivity;
    private TabLayout mTabLayout = null;

    public ViewPagerAdapter(AppCompatActivity activity, ArrayList<CustomFragment> fragments) {
        super(activity.getSupportFragmentManager(), activity.getLifecycle());
        mActivity = activity;
        mFragments = fragments;
    }

    @NonNull
    @Override
    public CustomFragment createFragment(int position) {
        CustomFragment fragment = mFragments.get(position);
        if (fragment.getViewPagerAdapter() == null) fragment.setViewPagerAdapter(this);
        if (fragment.getPosition() == 0) fragment.setPosition(position);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public void notifyDataSetChanged(int position) {

        if (getTabLayout() != null && getTabLayout().getSelectedTabPosition() != position) return;
        if (mActivity != null && mActivity instanceof MainActivity) {
            String title = this.createFragment(position).getTitle();
            String subtitle = this.createFragment(position).getSubtitle();
            ((MainActivity) mActivity).setCustomTitle(title);
            ((MainActivity) mActivity).setCustomSubtitle(subtitle);
        } else if (mActivity != null && mActivity instanceof UserLoanActivity) {

            ((UserLoanActivity) mActivity).setItemCount(this.createFragment(position).getSubtitle());
        }
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    public void setTabLayout(TabLayout tabLayout) {
        mTabLayout = tabLayout;
    }
}
