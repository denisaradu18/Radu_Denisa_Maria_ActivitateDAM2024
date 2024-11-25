package com.example.seminar4;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class ListaMasiniActivity extends AppCompatActivity {
    private ArrayList<Masina> masini;
    private int idModificat=0;
    private MasinaAdapter adapter=null;

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

        ListView lv=findViewById(R.id.masiniLV);
        masini=getIntent().getParcelableArrayListExtra("masina");
        if(masini!=null) {
            //ArrayAdapter<Masina> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, masini);
            //lv.setAdapter(adapter);
            adapter = new MasinaAdapter(masini, getApplicationContext(), R.layout.items_masina);
            lv.setAdapter(adapter);


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

                    masini.remove(i);
                    adapter.notifyDataSetChanged();
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
            masini.set(idModificat, data.getParcelableExtra("masini"));
            adapter.notifyDataSetChanged();
        }
    }
}