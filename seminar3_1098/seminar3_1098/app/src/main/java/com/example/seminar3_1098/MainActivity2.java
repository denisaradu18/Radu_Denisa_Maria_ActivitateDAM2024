package com.example.seminar3_1098;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    //bundle -> metadata atasate la intent
    //obiectele -> serializabile ca sa fie transmise intre acivitati
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

        //aceasta activitate va fi deschisa prin intermediul unui intent

        Intent it = getIntent();
        String text = it.getStringExtra("text");
        int val1 = it.getIntExtra("valoare 1",0);//in caz ca nu gaseste cheia,ia valoarea default
        int val2 = it.getIntExtra("valoare 2",0);

        Toast.makeText(this,"Suma este:"+ (val1+ val2),Toast.LENGTH_LONG).show();

    }
}