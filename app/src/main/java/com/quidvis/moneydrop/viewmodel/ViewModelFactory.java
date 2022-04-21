package com.quidvis.moneydrop.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.quidvis.moneydrop.database.source.BankAccountDataSource;

import org.jetbrains.annotations.NotNull;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final BankAccountDataSource bankAccountDataSource;

    public ViewModelFactory(BankAccountDataSource bankAccountDataSource) {
        this.bankAccountDataSource = bankAccountDataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull @NotNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BankAccountViewModel.class)) {
            return (T) new BankAccountViewModel(bankAccountDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
