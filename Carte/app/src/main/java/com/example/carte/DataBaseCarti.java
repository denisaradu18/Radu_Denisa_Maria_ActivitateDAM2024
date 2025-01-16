package com.example.carte;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Carte.class}, version = 1)
public abstract class DataBaseCarti extends RoomDatabase {
    public  abstract DAO daoCarte();
}
