package com.example.apstudent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdaugaStud extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adauga_stud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

       Spinner spinner=findViewById(R.id.spinner);
       ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(
               this,
               R.array.an,
               android.R.layout.simple_spinner_dropdown_item
       );
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       spinner.setAdapter(adapter);


      Spinner spinner1=findViewById(R.id.spinner2);
      ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(
              getApplicationContext(),
              R.array.curs,
              android.R.layout.simple_spinner_dropdown_item
      );
      adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner1.setAdapter(adapter1);

        Button bt=findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etnume=findViewById(R.id.editTextText);
                String nume=etnume.getText().toString();

//                RadioGroup rg=findViewById(R.id.radiogrup);
//                int id=rg.getCheckedRadioButtonId();
//                RadioButton rb=findViewById(id);
//                String sex=rb.getText().toString();



                RadioGroup rg=findViewById(R.id.radiogrup);
                int id=rg.getCheckedRadioButtonId();
                RadioButton rb=findViewById(id);
                String sex=rb.getText().toString();

                String anS=spinner.getSelectedItem().toString();
                int an=Integer.parseInt(anS);

                String curs=spinner1.getSelectedItem().toString();

                RatingBar ratingBar=findViewById(R.id.ratingBar);
                float rating=ratingBar.getRating();

                Student student=new Student(nume,sex,an,curs,rating);
                Toast.makeText(getApplicationContext(), student.toString(), Toast.LENGTH_LONG).show();


                Intent it=new Intent();
                it.putExtra("student", student);
                setResult(RESULT_OK, it);
                finish();


            }
        });
    }
}