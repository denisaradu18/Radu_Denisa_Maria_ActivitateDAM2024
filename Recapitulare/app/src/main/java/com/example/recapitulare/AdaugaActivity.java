package com.example.recapitulare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdaugaActivity extends AppCompatActivity {

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

        Spinner spinner=(Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(
                this,
                R.array.nivel_studiu,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        Button btn=findViewById(R.id.buttonAdauga);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edNume=findViewById(R.id.editTextNume);
                String nume=edNume.getText().toString();

                EditText edMedie=findViewById(R.id.editTextMedie);
                String smedie=edMedie.getText().toString();
                double medie=Double.parseDouble(smedie);

                EditText edAn=findViewById(R.id.editTextAn);
                String sAn=edAn.getText().toString();
                int an=Integer.parseInt(sAn);

                CheckBox cb=findViewById(R.id.checkBox);
                Boolean esteIntegralist=cb.isChecked();

                String nivInv=(String) spinner.getSelectedItem();

                Student stud=new Student(nume, medie,an, esteIntegralist,nivInv);

                Toast.makeText(AdaugaActivity.this, stud.toString(), Toast.LENGTH_LONG).show();

                Intent it=new Intent();
                it.putExtra("student", stud);
                setResult(RESULT_OK, it);
                finish();
            }
        });



    }
}