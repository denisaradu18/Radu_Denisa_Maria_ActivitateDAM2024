package com.example.seminar4;

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

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn2=findViewById(R.id.button3);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText tbModel=findViewById(R.id.tbModel);
                String model=tbModel.getText().toString();

                EditText tbAn=findViewById(R.id.tbAn);
                String sAn=tbAn.getText().toString();
                int An=Integer.parseInt(sAn);

                EditText tbPret=findViewById(R.id.tbPret);
                String sPret=tbPret.getText().toString();
                float pret= Float.parseFloat(sPret);

                EditText tbMarca=findViewById(R.id.tbMarca);
                String marca=tbMarca.getText().toString();

                CheckBox cbDa = findViewById(R.id.cbDa);
                Boolean esteNoua = cbDa.isChecked();


                Spinner spin=findViewById(R.id.spinner2);
                String alesSpin=(String) spin.getSelectedItem();

                Masina masina=new Masina(model, An,pret, marca,esteNoua,alesSpin);

                Intent it=new Intent();
                it.putExtra("Masina", masina);
                setResult(RESULT_OK,it);
                finish();

            }


        });


    }


}