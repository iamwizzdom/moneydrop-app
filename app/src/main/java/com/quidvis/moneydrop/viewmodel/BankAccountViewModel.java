package com.quidvis.moneydrop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.quidvis.moneydrop.database.source.BankAccountDataSource;
import com.quidvis.moneydrop.model.BankAccount;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BankAccountViewModel extends ViewModel {

    private final BankAccountDataSource bankAccountDataSource;
    private final LiveData<List<BankAccount>> bankAccounts;

    public BankAccountViewModel(BankAccountDataSource bankAccountDataSource) {
        this.bankAccountDataSource = bankAccountDataSource;
        bankAccounts = bankAccountDataSource.getBankAccounts();
    }

    public LiveData<List<BankAccount>> getBankAccounts() {
        return bankAccounts;
    }

    public void insertBankAccount(BankAccount movie) {
        this.bankAccountDataSource.insertBankAccount(movie);
    }

    public void updateBankAccount(BankAccount movie) {
        this.bankAccountDataSource.updateBankAccount(movie);
    }

    public void deleteBankAccount(BankAccount movie) {
        this.bankAccountDataSource.deleteBankAccount(movie);
    }

    public void deleteAllBankAccounts() {
        this.bankAccountDataSource.deleteAllBankAccounts();
    }

    public void getBankAccountsOnline() {

        deleteAllBankAccounts();

//        APIService apiService = RetroInstance.getInstance().create(APIService.class);
//        Call<BankAccountList> call = apiService.getBankAccountList();
//        call.enqueue(new Callback<BankAccountList>() {
//            @Override
//            public void onResponse(@NotNull Call<BankAccountList> call, @NotNull Response<BankAccountList> response) {
//                BankAccountList movies = response.body();
//                assert movies != null;
//                for (BankAccount movie: movies.getBankAccounts()) insertBankAccount(movie);
//                if (BankAccountViewModel.this.apiCallBack != null) BankAccountViewModel.this.apiCallBack.onComplete();
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<BankAccountList> call, @NotNull Throwable t) {
//                if (BankAccountViewModel.this.apiCallBack != null) BankAccountViewModel.this.apiCallBack.onFailure(t);
//            }
//        });
    }

}
