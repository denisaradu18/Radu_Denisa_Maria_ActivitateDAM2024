package com.example.invat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    List<Obiect> obiecteList=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        obiecteList=new ArrayList<>();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn=findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it=new Intent(getApplicationContext(), AdaugaObiect.class);
                 startActivityForResult(it,200);
            }
        });

        Button btn2=findViewById(R.id.button3);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it=new Intent(getApplicationContext(), MainActivity2.class);
                it.putExtra("pret",30);
                startActivityForResult(it,200);
            }
        });

        Button listaBtn=findViewById(R.id.button4);
        listaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent it=new Intent(getApplicationContext(), ListaObiecte.class);
              it.putParcelableArrayListExtra("obiect",(ArrayList<? extends Parcelable>) obiecteList);
              startActivity(it);
            }
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==200&&resultCode==RESULT_OK){

            Obiect ob=data.getParcelableExtra("obiect");
            obiecteList.add(ob);

//            if(ob!=null) {
//                String nume = ob.getNume();
//                long calendar = ob.getData();
//
//                TextView textViewNume=findViewById(R.id.textViewNume);
//                TextView textViewC=findViewById(R.id.textView3);
//
//                textViewNume.setText("NUME "+nume);
//
//                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//                String formattedDate = dateFormat.format(new Date(calendar));
//                textViewC.setText("Data:"+formattedDate);
//
//
//            }

        }
    }
}