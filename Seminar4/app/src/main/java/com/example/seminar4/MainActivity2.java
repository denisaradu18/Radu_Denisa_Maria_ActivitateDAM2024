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
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    BazaDeDate database;
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

        database = Room.databaseBuilder(getApplicationContext(), BazaDeDate.class,"masini_db").build();


        Intent it=getIntent();
        if(it.hasExtra("masina"));
        {
            Masina masina=it.getParcelableExtra("masina");
            EditText modelEt=findViewById(R.id.tbModel);
            EditText anEt=findViewById(R.id.tbAn);
            EditText pretEt=findViewById(R.id.tbPret);
            CheckBox esteNouEt=findViewById(R.id.cbDa);


            assert masina != null;
            modelEt.setText(masina.getModel());
            anEt.setText(masina.getAnFabricatie());
            pretEt.setText(masina.getPret());
            esteNouEt.setChecked(masina.isEsteNou());


        }
        Spinner spin=findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                MainActivity2.this,
                R.array.Tip_combustibil,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(adapter);

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
                int pret= Integer.parseInt(sPret);

                EditText tbMarca=findViewById(R.id.tbMarca);
                String marca=tbMarca.getText().toString();

                CheckBox cbDa = findViewById(R.id.cbDa);
                Boolean esteNoua = cbDa.isChecked();

                CheckBox estedidpOnline = findViewById(R.id.dispOnlinecb);
                Boolean esteBifat = estedidpOnline.isChecked();


                String alesSpin=(String) spin.getSelectedItem();


                Masina masina=new Masina(model, An,pret, marca,esteNoua,alesSpin);

                if (esteBifat) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("masini");
                    ref.push().setValue(masina)
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity2.this, "Obiect salvat în Firebase!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity2.this, "Eroare" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                Toast.makeText(MainActivity2.this, masina.toString(), Toast.LENGTH_LONG).show();
                Intent it=new Intent();
                it.putExtra("masina", masina);
                setResult(RESULT_OK,it);
                finish();
            }

        });
    }
}