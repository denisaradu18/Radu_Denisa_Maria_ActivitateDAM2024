package com.example.seminar14;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

import androidx.annotation.NonNull;

public class Grafica extends View {

    public Grafica(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        Paint  marker=new Paint();
        marker.setColor(Color.GREEN);
        marker.setStrokeWidth(20);
        marker.setStyle(Paint.Style.STROKE);


        canvas.drawLine(0,0,200,200,marker);

        marker.setColor(Color.argb(80,0,0,170));
        canvas.drawRect(200,200,600,600,marker);
        marker.setStyle(Paint.Style.FILL_AND_STROKE);
        marker.setColor(Color.RED);


        LinearGradient gradient =new LinearGradient(0,600,700,1100,Color.BLUE,Color.YELLOW, Shader.TileMode.MIRROR);
        Paint markerGradien=new Paint();
        markerGradien.setShader(gradient);

        canvas.drawCircle(200,800,200,markerGradien);

        marker.setTextSize(38);
        marker.setStrokeWidth(2);
        canvas.drawText("Text drept", 300, 1020,marker);

        Path cale=new Path();
        cale.addCircle(500, 1500,200,Path.Direction.CW);
        canvas.drawTextOnPath("text Curbat", cale,0,0,marker);

        marker.setColor(Color.BLUE);
        canvas.drawArc(0,1200,400,1600,40,46,true,marker);




    }

}
