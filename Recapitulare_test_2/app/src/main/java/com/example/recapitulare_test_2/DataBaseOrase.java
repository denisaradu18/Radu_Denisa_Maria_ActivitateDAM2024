package com.example.recapitulare_test_2;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities ={Oras.class},version = 1)
public abstract class DataBaseOrase extends RoomDatabase {
    public abstract DAO Daoorase();
}
