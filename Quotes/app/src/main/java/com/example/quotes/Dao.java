package com.example.quotes;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@androidx.room.Dao
public interface Dao {
    @Insert
    void insert(Citat citat);

    @Update
    void update(Citat citat);

    @Delete
    void delete(Citat citat);

    @Query("SELECT * FROM Citat")
    List<Citat> select();
}
