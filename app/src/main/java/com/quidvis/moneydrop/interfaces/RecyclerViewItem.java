package com.quidvis.moneydrop.interfaces;

public interface RecyclerViewItem {

    int VIEW_TYPE_HEADER = 0;
    int VIEW_TYPE_ITEM = 1;
    int VIEW_TYPE_LOADING = 2;
    int VIEW_TYPE_LOADING_MORE = 3;
    int VIEW_TYPE_NO_CONTENT = 4;
    int VIEW_TYPE_NO_MORE_RECORD = 5;

    public int getItemType();
}
