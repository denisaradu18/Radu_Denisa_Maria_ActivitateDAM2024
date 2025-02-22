package com.example.quotes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdaugaActivity extends AppCompatActivity {



    DataBase dataBase;

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
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource( getApplicationContext(),
                R.array.Gen,
                android.R.layout.simple_spinner_dropdown_item
                );
        spinner.setAdapter(adapter);
            Executor executor =Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

        dataBase=Room.databaseBuilder(getApplicationContext(), DataBase.class, "citate.db").build();


        Intent it=getIntent();
        if(it.hasExtra("citat"))
        {
            Citat c=it.getParcelableExtra("citat");

            EditText edText=findViewById(R.id.editTextText);
            EditText edautor=findViewById(R.id.editTextText2);
            Spinner sgen=findViewById(R.id.spinner);

            edText.setText(c.getText());
            edautor.setText(c.getAutor());
            sgen.setSelection(((ArrayAdapter<CharSequence>) sgen.getAdapter()).getPosition(c.getGen()));
        }

        Button btn=findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edText=findViewById(R.id.editTextText);
                String tetx=edText.getText().toString();

                EditText edautor=findViewById(R.id.editTextText2);
                String autor=edautor.getText().toString();

                String gen=spinner.getSelectedItem().toString();

                Citat citat=new Citat(tetx, autor, gen);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            FileOutputStream file=openFileOutput("fisier.txt",MODE_APPEND);
                            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(file);
                            BufferedWriter writer=new BufferedWriter(outputStreamWriter);
                            writer.write(citat.toString());
                            writer.close();;
                            file.close();
                            outputStreamWriter.close();

                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                        dataBase.daoCitat().insert(citat);
                    }
                });

                Intent it=new Intent();
                it.putExtra("citat",citat);
                setResult(RESULT_OK,it);
                finish();

            }
        });
    }
}