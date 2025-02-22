package com.example.quotes;

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
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListaActivity extends AppCompatActivity {

    DataBase dataBase;
    List<Citat> listCitate=new ArrayList<>();
    ArrayAdapter<Citat> adapter;

    private int idModificat=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lv=findViewById(R.id.listView);

        dataBase=Room.databaseBuilder(getApplicationContext(), DataBase.class, "citate.db").build();
        Executor executor=Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listCitate=dataBase.daoCitat().select();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter=new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_list_item_1,
                                listCitate);
                        lv.setAdapter(adapter);
                    }
                });
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent itModificat=new Intent(getApplicationContext(), AdaugaActivity.class);
                itModificat.putExtra("citat",listCitate.get(position));
                idModificat=1;
                startActivityForResult(itModificat,200);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //Executor executor=Executors.newSingleThreadExecutor();
                //Handler handler=new Handler(Looper.myLooper());
                //executor.execute(new Runnable() {
                  //  @Override
                    //public void run() {
                      //  dataBase.daoCitat().delete(listCitate.get(position));
                        //handler.post(new Runnable() {
                          //  @Override
                            //public void run() {
                              //  listCitate.remove(position);
                                //adapter.notifyDataSetChanged();
                     //       }
                   //     });
                 //   }
               // });

                //listCitate.remove(position);
                //adapter.notifyDataSetChanged();


               SharedPreferences sp=getSharedPreferences("fisier", MODE_PRIVATE);
               SharedPreferences.Editor edit=sp.edit();
               edit.putString(listCitate.get(position).getKey(), listCitate.get(position).toString());
               edit.commit();
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==200&& resultCode==RESULT_OK)
        {
            Citat citatModificat=data.getParcelableExtra("citat");
            Executor executor=Executors.newSingleThreadExecutor();
            Handler handler=new Handler(Looper.myLooper());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    dataBase.daoCitat().update(citatModificat);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listCitate.set(idModificat,citatModificat);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });

        }
    }
}