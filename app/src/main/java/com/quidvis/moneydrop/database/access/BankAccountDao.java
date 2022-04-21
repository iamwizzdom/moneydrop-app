package com.quidvis.moneydrop.database.access;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.quidvis.moneydrop.model.BankAccount;

import java.util.List;

@androidx.room.Dao
public interface BankAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBankAccount(BankAccount model);


    @Update
    void updateBankAccount(BankAccount model);


    @Delete
    void deleteBankAccount(BankAccount model);


    @Query("DELETE FROM bank_accounts")
    void deleteAllBankAccounts();


    @Query("SELECT * FROM bank_accounts ORDER BY id DESC")
    LiveData<List<BankAccount>> getAllBankAccounts();

}
