package com.example.recapitulare_test_2;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DAO {
    @Insert
    void insert(Oras oras);

    @Update
    void updateOras(Oras oras);

    @Query("select * from oras")
    List<Oras> getOrase();


    @Delete
    void delete(Oras oras);

}
