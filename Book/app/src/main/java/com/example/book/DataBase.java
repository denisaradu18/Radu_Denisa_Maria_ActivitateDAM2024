package com.example.book;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataBase extends AppCompatActivity {

    DataBaseBook dataBase;
    List<Book> bookList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lv=findViewById(R.id.listView);

        dataBase= Room.databaseBuilder(getBaseContext(), DataBaseBook.class, "books.db").build();
        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                bookList=dataBase.daoBook().select();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<Book> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,
                                bookList);
                        lv.setAdapter(adapter);
                    }
                });


            }
        });
    }
}