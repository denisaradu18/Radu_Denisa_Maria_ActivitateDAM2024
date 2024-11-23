package com.example.recap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivitateAdaugareCalculator extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activitate_adaugare_calculator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinner=(Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(
                this,
                R.array.tip_procesor,
                android.R.layout.simple_spinner_dropdown_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        RadioGroup radioGroup=findViewById(R.id.radiogrup);


        Button bt=findViewById(R.id.button4);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText denumire=findViewById(R.id.edDenumire);
                String denumires=denumire.getText().toString();

                EditText eMemorie=findViewById(R.id.edMemorie);
                String sMemorie=eMemorie.getText().toString();
                int memorie=Integer.parseInt(sMemorie);

                String tipProcesor=(String)  spinner.getSelectedItem();

                CheckBox cb=findViewById(R.id.checkBox);
                Boolean areInstalat=cb.isChecked();



                Calculator calculator=new Calculator(denumires,memorie,tipProcesor,areInstalat);

                Intent it=new Intent();
                it.putExtra("calculator",calculator);
                setResult(RESULT_OK,it);
                finish();


            }
        });
    }
}