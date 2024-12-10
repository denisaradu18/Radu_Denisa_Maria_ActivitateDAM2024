package com.example.seminar4;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoMasina {
    @Insert
    void insert(Masina masina);

    @Query("DELETE FROM Masina WHERE anFabricatie=:anFabricatie ")
    void deleteMasina(int anFabricatie);

    @Query("SELECT * FROM Masina")
    List<Masina> getListaMasini();

    @Update
    void update(Masina masini);

}
