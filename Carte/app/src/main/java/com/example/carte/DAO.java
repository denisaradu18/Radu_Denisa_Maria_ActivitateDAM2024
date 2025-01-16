package com.example.carte;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface DAO {
    @Insert
    public void insert(Carte carte);

    @Query("select * from Carti")
    List<Carte> selectCarti();

    @Delete
    public void delete(Carte carte);

    @Update
    public  void update(Carte carte);
}
