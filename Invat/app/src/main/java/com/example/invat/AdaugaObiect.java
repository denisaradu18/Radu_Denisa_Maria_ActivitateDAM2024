package com.example.invat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdaugaObiect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adauga_obiect);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

      Spinner spinner=(Spinner) findViewById(R.id.spinner);
      ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(
              this,
              R.array.tip_obiect,
              android.R.layout.simple_spinner_dropdown_item
      );
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(adapter);



        Button btnAdauga=findViewById(R.id.button2);
        btnAdauga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edNume=findViewById(R.id.editTextText);
                String nume=edNume.getText().toString();

                EditText edpret=findViewById(R.id.editTextText2);
                String spret=edpret.getText().toString();
                float pret=Float.parseFloat(spret);

                CalendarView calendarView=findViewById(R.id.calendarView);
                long calendar=calendarView.getDate();

                String ales=(String) spinner.getSelectedItem();

                Obiect obiect=new Obiect(nume,pret,calendar,ales);

                Intent it=new Intent();
                it.putExtra("obiect",obiect);
                setResult(RESULT_OK,it);
                finish();



            }
        });

    }
}