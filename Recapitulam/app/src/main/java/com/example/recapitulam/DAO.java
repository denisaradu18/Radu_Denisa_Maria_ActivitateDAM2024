package com.example.recapitulam;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DAO {
    @Query("Select * from Cursa")
    List<Cursa> selectall();

    @Insert
    void insert(Cursa cursa);

    @Delete
    public void delete(Cursa c);
}
