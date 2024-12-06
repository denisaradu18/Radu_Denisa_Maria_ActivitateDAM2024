package com.example.seminar4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Afisareimg extends AppCompatActivity {
    private List<ImaginiDomeniu> imaginiDomeniuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_afisareimg);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<String> linkuriImagini = new ArrayList<>();
        linkuriImagini.add("https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg");
        linkuriImagini.add("https://images.pexels.com/photos/3874339/pexels-photo-3874339.jpeg");
        linkuriImagini.add("https://images.pexels.com/photos/1149831/pexels-photo-1149831.jpeg");
        linkuriImagini.add("https://images.pexels.com/photos/1230795/pexels-photo-1230795.jpeg");
        linkuriImagini.add("https://images.pexels.com/photos/358070/pexels-photo-358070.jpeg");

        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());
//
        List<Bitmap> imagini=new ArrayList<>();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                for(String link:linkuriImagini){
                    HttpURLConnection con=null;
                    try {
                        URL url=new URL(link);
                        con=(HttpURLConnection) url.openConnection();
                        InputStream is=con.getInputStream();
                        imagini.add(BitmapFactory.decodeStream(is));

                    }catch( IOException e){
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imaginiDomeniuList = new ArrayList<>();
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 1", imagini.get(0), "https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 2", imagini.get(1), "https://images.pexels.com/photos/3874339/pexels-photo-3874339.jpeg"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 3", imagini.get(2), "https://images.pexels.com/photos/1149831/pexels-photo-1149831.jpeg"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 4", imagini.get(3), "https://images.pexels.com/photos/1230795/pexels-photo-1230795.jpeg"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 5", imagini.get(4), "https://images.pexels.com/photos/358070/pexels-photo-358070.jpeg"));



                            ListView lv=findViewById(R.id.listViewImagini);
                            ImageAdapter adapter=new ImageAdapter(imaginiDomeniuList, getApplicationContext(), R.layout.image_layout);
                            lv.setAdapter(adapter);

                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent it=new Intent(getApplicationContext(), WebViewActivity.class);
                                    it.putExtra("link",imaginiDomeniuList.get(position).getLink());
                                    startActivity(it);
                                }
                            });
                        }
                    });
                }
            }
        });


    }
}