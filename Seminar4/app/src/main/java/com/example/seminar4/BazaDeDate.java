package com.example.seminar4;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities ={Masina.class}, version = 1)
public abstract class BazaDeDate extends RoomDatabase {
   public abstract DaoMasina getDaoObject();


}
