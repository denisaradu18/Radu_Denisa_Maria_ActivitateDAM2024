package com.example.seminar4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private List<ImaginiDomeniu> masini=null;
    private Context ctx;
    private int resurseLayout;


    public ImageAdapter(List<ImaginiDomeniu> masini, Context ctx, int resurseLayout) {
        this.masini = masini;
        this.ctx = ctx;
        this.resurseLayout = resurseLayout;
    }

    @Override
    public int getCount() {
        return masini.size();
    }

    @Override
    public Object getItem(int i) {
        return masini.get(i);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(ctx);
        View v=inflater.inflate(resurseLayout,parent,false);

        ImageView imageView=v.findViewById(R.id.imageView2);
        TextView textView=v.findViewById(R.id.textView3);

        ImaginiDomeniu imaginiDomeniu=(ImaginiDomeniu) getItem(i) ;

        imageView.setImageBitmap(imaginiDomeniu.getImagine());
        textView.setText(imaginiDomeniu.getTextAfisat());


        return null;
    }
}
