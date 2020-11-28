package com.quidvis.moneydrop.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoansViewModel extends ViewModel {
    // TODO: Implement the ViewModel

    /**
     * Live Data Instance
     */
    private final MutableLiveData<String> mName = new MutableLiveData<>();
    public void setName(String name) {
        mName.setValue(name);
    }
    public LiveData<String> getName() {
        return mName;
    }
}