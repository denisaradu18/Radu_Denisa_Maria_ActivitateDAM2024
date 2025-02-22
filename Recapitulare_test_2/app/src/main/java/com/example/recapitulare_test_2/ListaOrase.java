package com.example.recapitulare_test_2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListaOrase extends AppCompatActivity {


    private List<Oras> orase=null;
    private DataBaseOrase dataBaseOrase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_orase);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dataBaseOrase = Room.databaseBuilder(getApplicationContext(), DataBaseOrase.class,"orase.db").build();
        ListView lvOrase=findViewById(R.id.listView);

        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                orase=dataBaseOrase.Daoorase().getOrase();
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        ArrayAdapter<Oras> adapter=new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_list_item_1,
                                orase);
                        lvOrase.setAdapter(adapter);
                    }
                });
            }
        });

        lvOrase.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent
            }
        });

    }
}