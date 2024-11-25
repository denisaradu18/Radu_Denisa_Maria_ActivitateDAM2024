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

        List<String> linkuriImagini=new ArrayList<>();
        linkuriImagini.add("https://www.pexels.com/photo/blue-bmw-sedan-near-green-lawn-grass-170811/");
        linkuriImagini.add("https://www.freepik.com/free-photo/white-offroader-jeep-parking_5895941.htm#fromView=keyword&page=1&position=4&uuid=dd783442-54e9-4a0c-8fe5-36205e3193af");
        linkuriImagini.add("https://www.freepik.com/free-photo/front-view-black-luxury-sedan-road_6159491.htm#fromView=keyword&page=1&position=14&uuid=dd783442-54e9-4a0c-8fe5-36205e3193af");
        linkuriImagini.add("https://www.freepik.com/free-photo/man-sitting-car-hood_5790291.htm#fromView=keyword&page=1&position=24&uuid=966e5ade-b413-4470-80ef-ea35a2fd0cce");
        linkuriImagini.add("https://www.freepik.com/free-photo/vintage-sedan-car-driving-highway-side-view_6159528.htm#fromView=keyword&page=1&position=2&uuid=1c82cc79-65c9-4fa6-a9ac-4fc6bc287ac6");

        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

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
                            imaginiDomeniuList=new ArrayList<>();
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 1", imagini.get(0),"https://www.pexels.com/photo/blue-bmw-sedan-near-green-lawn-grass-170811/"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 2", imagini.get(1),"https://www.freepik.com/free-photo/white-offroader-jeep-parking_5895941.htm#fromView=keyword&page=1&position=4&uuid=dd783442-54e9-4a0c-8fe5-36205e3193af"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 3", imagini.get(2),"https://www.freepik.com/free-photo/front-view-black-luxury-sedan-road_6159491.htm#fromView=keyword&page=1&position=14&uuid=dd783442-54e9-4a0c-8fe5-36205e3193af"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 4", imagini.get(3),"https://www.freepik.com/free-photo/man-sitting-car-hood_5790291.htm#fromView=keyword&page=1&position=24&uuid=966e5ade-b413-4470-80ef-ea35a2fd0cce"));
                            imaginiDomeniuList.add(new ImaginiDomeniu("Masina 5", imagini.get(4),"https://www.freepik.com/free-photo/vintage-sedan-car-driving-highway-side-view_6159528.htm#fromView=keyword&page=1&position=2&uuid=1c82cc79-65c9-4fa6-a9ac-4fc6bc287ac6"));

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