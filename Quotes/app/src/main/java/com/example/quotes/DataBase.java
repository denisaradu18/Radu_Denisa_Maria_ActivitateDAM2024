package com.example.quotes;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Citat.class}, version = 1)
public abstract class DataBase extends RoomDatabase {
    abstract Dao daoCitat();
}
