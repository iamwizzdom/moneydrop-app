package com.quidvis.moneydrop.database.source;

import androidx.lifecycle.LiveData;

import com.quidvis.moneydrop.database.access.BankAccountDao;
import com.quidvis.moneydrop.model.BankAccount;

import java.util.List;

public class BankAccountDataSource {

    private final BankAccountDao movieDao;

    public BankAccountDataSource(BankAccountDao movieDao) {
        this.movieDao = movieDao;
    }

    public LiveData<List<BankAccount>> getBankAccounts() {
        return this.movieDao.getAllBankAccounts();
    }

    public void insertBankAccount(BankAccount movie) {
        new Thread(() -> BankAccountDataSource.this.movieDao.insertBankAccount(movie)).start();
    }

    public void updateBankAccount(BankAccount movie) {
        new Thread(() -> BankAccountDataSource.this.movieDao.updateBankAccount(movie)).start();
    }

    public void deleteBankAccount(BankAccount movie) {
        new Thread(() -> BankAccountDataSource.this.movieDao.deleteBankAccount(movie)).start();
    }

    public void deleteAllBankAccounts() {
        new Thread(BankAccountDataSource.this.movieDao::deleteAllBankAccounts).start();
    }
}
