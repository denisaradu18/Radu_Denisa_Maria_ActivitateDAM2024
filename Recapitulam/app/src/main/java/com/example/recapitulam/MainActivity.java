package com.example.recapitulam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    List<Cursa> cursaList;
    DataBaseCursa dataBaseCursa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cursaList=new ArrayList<>();


        dataBaseCursa= Room.databaseBuilder(getApplicationContext(), DataBaseCursa.class, "curse.db").build();
        Executor executor= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

        Button btnAdauga=findViewById(R.id.btn_adauga);
        btnAdauga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edId=findViewById(R.id.editTextText);
                String sid=edId.getText().toString();
                int id=Integer.parseInt(sid);

                EditText edDestinatie=findViewById(R.id.editTextText2);
                String sdestinatie=edDestinatie.getText().toString();

                EditText distanta=findViewById(R.id.editTextText3);
                String sdistanta=distanta.getText().toString();
                int distanaa=Integer.parseInt(sdistanta);
                Boolean esteManual = Boolean.valueOf("true");

                Cursa cursa=new Cursa(id,sdestinatie,distanaa, esteManual);
                cursaList.add(cursa);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        dataBaseCursa.daoCursa().insert(cursa);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb=new StringBuilder();
                                for(Cursa c:cursaList)
                                {
                                    sb.append(c.toString());

                                }

                                EditText ed=findViewById(R.id.editTextText4);
                                ed.setText(sb);

                            }
                        });
                    }
                });





            }
        });

        Button btnIncarca=findViewById(R.id.btn_incarca);
        btnIncarca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String urlJson="https://pdm.ase.ro/curse.json";
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        StringBuilder rezultat=new StringBuilder();
                        HttpsURLConnection con=null;
                        try {
                            URL url=new URL(urlJson);
                            con=(HttpsURLConnection) url.openConnection();
                            con.setRequestMethod("GET");
                            con.connect();

                            InputStreamReader isr=new InputStreamReader(con.getInputStream());
                            BufferedReader reader=new BufferedReader(isr);
                            StringBuilder sb=new StringBuilder();
                            String linie=null;
                            while ((linie=reader.readLine())!=null)
                            {
                                sb.append(linie);
                            }

                            JSONObject jsonObject=new JSONObject(sb.toString());
                            JSONArray jsonArray=jsonObject.getJSONArray("cursa");
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject cursa=jsonArray.getJSONObject(i);
                                int id=cursa.getInt("id");
                                String destinatie=cursa.getString("destinatie");
                                int distanta=cursa.getInt("distanta");
                                Boolean esteManual = Boolean.valueOf("false");

                                Cursa c = new Cursa(id,destinatie,distanta,esteManual);

                                dataBaseCursa.daoCursa().insert(c);
                                rezultat.append(c.toString());
                            }
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                               EditText editText=findViewById(R.id.editTextText4);
                               editText.setText(rezultat.toString());
                            }
                        });

                    }
                });
            }
        });


        Button btst=findViewById(R.id.btn_sterge);
        btst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                      List<Cursa> listaManuale=new ArrayList<>();
                      for(Cursa c:cursaList)
                      {
                          if(c.getEsteManuala()){
                              dataBaseCursa.daoCursa().delete(c);
                              listaManuale.add(c);
                          }
                      }
                        cursaList.remove(listaManuale);

                    }
                });
            }
        });
    }
}