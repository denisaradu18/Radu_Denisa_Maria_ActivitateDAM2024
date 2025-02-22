package com.example.book;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.List;

@androidx.room.Dao
public interface Dao {
    @Insert
    void insert(Book book);

    @Query("select * from book")
    List<Book> select();

    @Delete
    void delte(Book book);
}
