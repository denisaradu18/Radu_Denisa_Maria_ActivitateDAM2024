package com.example.book;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Book.class}, version = 1)
public abstract class DataBaseBook extends RoomDatabase {
    public abstract Dao daoBook();
}
