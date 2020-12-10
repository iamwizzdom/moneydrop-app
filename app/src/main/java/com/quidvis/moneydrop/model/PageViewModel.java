package com.quidvis.moneydrop.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private final MutableLiveData<String> mName = new MutableLiveData<>();

    public void setName(String name) {
        mName.setValue(name);
    }

    public LiveData<String> getName() {
        return mName;
    }
}
