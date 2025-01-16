package com.example.carte;

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

public class ListaCarti extends AppCompatActivity {
    private List<Carte> listaCarti;
    private DataBaseCarti database;
    private ArrayAdapter<Carte>adapter=null;

    private int isModificat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_carti);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lv=findViewById(R.id.listView);
        database= Room.databaseBuilder(getApplicationContext(),DataBaseCarti.class,"Carti.db").build();

        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                listaCarti=database.daoCarte().selectCarti();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, listaCarti);
                        lv.setAdapter(adapter);
                    }
                });
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentModificat=new Intent(getApplicationContext(), AdaugaCarte.class);
                intentModificat.putExtra("carte", listaCarti.get(position));
                isModificat=position;
                startActivityForResult(intentModificat,200);
            }
        });





    }
}