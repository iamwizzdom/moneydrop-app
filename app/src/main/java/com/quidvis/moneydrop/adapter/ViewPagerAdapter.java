package com.quidvis.moneydrop.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private final ArrayList<CustomFragment> mFragments;
    private final FragmentActivity mActivity;
    private TabLayout mTabLayout = null;

    public ViewPagerAdapter(FragmentActivity activity, ArrayList<CustomFragment> fragments) {
        super(activity.getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mActivity = activity;
        mFragments = fragments;
    }

    @NonNull
    @Override
    public CustomFragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        CustomFragment fragment = mFragments.get(position);
        if (fragment.getViewPagerAdapter() == null) fragment.setViewPagerAdapter(this);
        if (fragment.getPosition() == 0) fragment.setPosition(position);
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle();
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return mFragments.size();
    }

    public void notifyDataSetChanged(int position) {
        if (getTabLayout() != null && getTabLayout().getSelectedTabPosition() != position) return;
        if (mActivity != null && mActivity instanceof MainActivity) {
            String title = this.getItem(position).getTitle();
            String subtitle = this.getItem(position).getSubtitle();
            ((MainActivity) mActivity).setCustomTitle(title);
            ((MainActivity) mActivity).setCustomSubtitle(subtitle);
        }
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    public void setTabLayout(TabLayout tabLayout) {
        mTabLayout = tabLayout;
    }
}
