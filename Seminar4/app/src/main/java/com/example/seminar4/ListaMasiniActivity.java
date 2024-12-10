package com.example.seminar4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Database;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListaMasiniActivity extends AppCompatActivity {
    private List<Masina> masini;
    private int idModificat=0;
    private MasinaAdapter adapter=null;
    BazaDeDate database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_masini);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });


        //Intent it=getIntent();
        //List<Masina> masini=it.getParcelableArrayListExtra("masini");

        database= Room.databaseBuilder(getApplicationContext(), BazaDeDate.class, "masini_db").build();
        ListView lv=findViewById(R.id.masiniLV);

        masini=new ArrayList<>();
        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());
        executor.execute(new Runnable() {
          @Override
           public void run() {
              masini=database.getDaoObject().getListaMasini();
              handler.post(new Runnable() {
                  @Override
                  public void run() {
                      adapter=new MasinaAdapter(masini, getApplicationContext(),R.layout.items_masina);
                      lv.setAdapter(adapter);
                  }
                });

               }
            });


       // masini=getIntent().getParcelableArrayListExtra("masina");
        if(masini!=null) {
            //ArrayAdapter<Masina> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, masini);
            //lv.setAdapter(adapter);
           // adapter = new MasinaAdapter(masini, getApplicationContext(), R.layout.items_masina);
            //lv.setAdapter(adapter);


            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    Intent intentModifica = new Intent(getApplicationContext(), MainActivity2.class);
                    intentModifica.putExtra("masina", masini.get(i));
                    idModificat = 1;
                    startActivityForResult(intentModifica, 209);
                    Toast.makeText(getApplicationContext(), masini.get(i).toString(), Toast.LENGTH_SHORT).show();
                }
            });


            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    //masini.remove(i);
                    //adapter.notifyDataSetChanged();
                    SharedPreferences sp=getSharedPreferences("obiecte", MODE_PRIVATE);
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putString(masini.get(i).getKey(), masini.get(i).toString());
                    editor.commit();
                    return false;
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && requestCode ==200)
        {
            Masina masinaModificata=data.getParcelableExtra("masina");

            Executor executor =Executors.newSingleThreadExecutor();
            Handler handler=new Handler(Looper.myLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    database.getDaoObject().update(masinaModificata);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            masini.set(idModificat, masinaModificata);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }
}