package com.quidvis.moneydrop.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.quidvis.moneydrop.database.access.BankAccountDao;
import com.quidvis.moneydrop.model.BankAccount;

//@androidx.room.Database(entities = {BankAccount.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {

    private static Database instance;

    public abstract BankAccountDao bankAccountDao();

    public static synchronized Database getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    Database.class, "moneydrop_db"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
