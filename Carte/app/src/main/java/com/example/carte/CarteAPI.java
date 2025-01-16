package com.example.carte;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class CarteAPI extends AppCompatActivity {

    //la apasarea butonului sa se afiseze titlul cartii care are isbn ul introdus
    String apikey = "AIzaSyBZxH-ryQ8I2_gUEoYX1L3WFWTl7TJUYJc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carte_api);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnAPI = findViewById(R.id.btnAPI);

        Executor executor=Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.myLooper());

        btnAPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edisbn=findViewById(R.id.edText_ISBN);
                String isbn=edisbn.getText().toString();

                String url="https://www.googleapis.com/books/v1/volumes?q=isbn:"+isbn+"&key="+apikey;

                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        HttpsURLConnection con=null;
                        StringBuilder booksdet=new StringBuilder();

                        try{
                            URL urlApi=new URL(url);
                            con=(HttpsURLConnection) urlApi.openConnection();
                            con.setRequestMethod("GET");
                            con.connect();


                            InputStreamReader isr=new InputStreamReader(con.getInputStream());
                            BufferedReader reader=new BufferedReader(isr);
                            StringBuilder builder=new StringBuilder();

                            String linie;

                            while((linie=reader.readLine())!=null)
                            {
                                builder.append(linie);
                            }
                            reader.close();
                            con.disconnect();

                            //parsarea JSON

                            JSONObject jsonObject=new JSONObject(builder.toString());
                            JSONArray items=jsonObject.getJSONArray("items");
                            JSONObject firstitems=items.getJSONObject(0);
                            JSONObject volumeInfo=firstitems.getJSONObject("volumeInfo");
                            String title=volumeInfo.getString("title");

                            booksdet.append("Titlul Cartii").append(title).append("\n");

                        } catch (ProtocolException e) {
                            throw new RuntimeException(e);
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
                                TextView carteTv=findViewById(R.id.textView2);
                                carteTv.setText(booksdet.toString());
                            }
                        });
                    }
                });

            }
        });

    }


}
