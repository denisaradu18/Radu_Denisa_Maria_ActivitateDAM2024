package com.example.recapitulare_test_2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdaugaActivity extends AppCompatActivity {

    DataBaseOrase dataBaseOrase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adauga);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Spinner spinner=findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(
                getApplicationContext(),
                R.array.Judete,
                android.R.layout.simple_spinner_dropdown_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        dataBaseOrase= Room.databaseBuilder(getApplicationContext(), DataBaseOrase.class, "orase.db").build();


        Button btn=findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edNume=findViewById(R.id.editTextText);
                String nume=edNume.getText().toString();

                EditText edLocutiori=findViewById(R.id.editTextText2);
                String slocuitori=edLocutiori.getText().toString();
                int locuitori=Integer.parseInt(slocuitori);

                String judet=spinner.getSelectedItem().toString();

                Oras oras=new Oras(nume,locuitori,judet);

                Executor executor= Executors.newSingleThreadExecutor();

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        dataBaseOrase.Daoorase().insert(oras);
                    }
                });


            }
        });
    }
}