package com.quidvis.moneydrop.fragment.custom;

import com.quidvis.moneydrop.adapter.ViewPagerAdapter;

public abstract class CustomFragment extends CustomCompatFragment {

    private int position;
    private String title, subtitle;
    private ViewPagerAdapter viewPagerAdapter;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public ViewPagerAdapter getViewPagerAdapter() {
        return viewPagerAdapter;
    }

    public void setViewPagerAdapter(ViewPagerAdapter viewPagerAdapter) {
        this.viewPagerAdapter = viewPagerAdapter;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public abstract void saveState();

    public abstract void refresh();

    public abstract void mount();

    public abstract void dismount();
}
