package com.example.seminar4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class MasinaAdapter extends BaseAdapter {
    private List<Masina> masini=null;
    private Context ctx;
    private int resureseLayout;

    public MasinaAdapter(List<Masina> masini, Context ctx, int resureseLayout) {
        this.masini = masini;
        this.ctx = ctx;
        this.resureseLayout = resureseLayout;
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
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(ctx);
        View v=inflater.inflate(resureseLayout, parent,false);
        TextView modelM=v.findViewById(R.id.modelM);
        TextView anFabricM=v.findViewById(R.id.an_fabricM);       TextView pretM=v.findViewById(R.id.pretM);
        TextView maracaM=v.findViewById(R.id.marcaM);
        CheckBox esteNouM=v.findViewById(R.id.esteNouM);
        TextView tipCombustibil=v.findViewById(R.id.tipCombustibilM);

        Masina masina=(Masina)getItem(i);

        //pentru int sau float
        //int a=7;
        //telefonTV.setText(String.valueof(a));
        modelM.setText(masina.getModel());
        anFabricM.setText(masina.getAnFabricatie());
        pretM.setText(masina.getAnFabricatie());
        maracaM.setText(masina.getMarca());
        esteNouM.setChecked(masina.isEsteNou());
        tipCombustibil.setText(masina.getTipCombustibil());

       return  v;

    }
}
