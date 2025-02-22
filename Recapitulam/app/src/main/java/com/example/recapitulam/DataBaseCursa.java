package com.example.recapitulam;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Cursa.class}, version = 1)
public abstract class DataBaseCursa extends RoomDatabase {
    public  abstract DAO daoCursa();
}
