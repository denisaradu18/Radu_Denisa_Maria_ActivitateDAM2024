package com.example.carte;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdaugaCarte extends AppCompatActivity {

    private DataBaseCarti database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adauga_carte);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database= Room.databaseBuilder(getApplicationContext(), DataBaseCarti.class, "Carti.db").build();

        Button btnAdauga=findViewById(R.id.btn_adauga);
        btnAdauga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edIsbn=findViewById(R.id.editTextText);
                String sIsbn=edIsbn.getText().toString();
                int isbn=Integer.parseInt(sIsbn);

                EditText edTitlu=findViewById(R.id.editTextText2);
                String titlu=edTitlu.getText().toString();

                EditText edGen=findViewById(R.id.editTextText3);
                String gen=edGen.getText().toString();

                Carte carte=new Carte(isbn,titlu,gen);

                Executor executor= Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream file=openFileOutput("carti.txt", MODE_APPEND);
                            OutputStreamWriter outpur=new OutputStreamWriter(file);
                            BufferedWriter writer=new BufferedWriter(outpur);

                            writer.write(carte.toString()+"\n");
                            writer.close();
                            outpur.close();
                            file.close();

                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        database.daoCarte().insert(carte);

                    }
                });

                Intent it=new Intent();
                it.putExtra("carte", carte);
                setResult(RESULT_OK, it);
                finish();

            }
        });
    }
}