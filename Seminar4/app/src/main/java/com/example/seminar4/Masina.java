package com.example.seminar4;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName="Masina")


public class Masina implements Parcelable {
    @PrimaryKey
    @NotNull
    private String model;
    private int anFabricatie;
    private int pret;
    private String marca;
    private boolean esteNou;

    private String tipCombustibil;


    public Masina(String model, int anFabricatie, int pret, String marca, boolean esteNou, String tipCombustibil) {
        this.model = model;
        this.anFabricatie = anFabricatie;
        this.pret = pret;
        this.marca = marca;
        this.esteNou = esteNou;
        this.tipCombustibil = tipCombustibil;
    }

    protected Masina(Parcel in) {
        model = in.readString();
        anFabricatie = in.readInt();
        pret = in.readInt();
        marca = in.readString();
        esteNou = in.readByte() != 0;
        tipCombustibil = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(model);
        dest.writeInt(anFabricatie);
        dest.writeFloat(pret);
        dest.writeString(marca);
        dest.writeByte((byte) (esteNou ? 1 : 0));
        dest.writeString(tipCombustibil);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Masina> CREATOR = new Creator<Masina>() {
        @Override
        public Masina createFromParcel(Parcel in) {
            return new Masina(in);
        }

        @Override
        public Masina[] newArray(int size) {
            return new Masina[size];
        }
    };

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getAnFabricatie() {
        return anFabricatie;
    }

    public void setAnFabricatie(int anFabricatie) {
        this.anFabricatie = anFabricatie;
    }

    public int getPret() {
        return pret;
    }

    public void setPret(int pret) {
        this.pret = pret;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public boolean isEsteNou() {
        return esteNou;
    }

    public void setEsteNou(boolean esteNou) {
        this.esteNou = esteNou;
    }

    public String getTipCombustibil() {
        return tipCombustibil;
    }

    public void setTipCombustibil(String tipCombustibil) {
        this.tipCombustibil = tipCombustibil;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Masina{");
        sb.append("model='").append(model).append('\'');
        sb.append(", anFabricatie=").append(anFabricatie);
        sb.append(", pret=").append(pret);
        sb.append(", marca='").append(marca).append('\'');
        sb.append(", esteNou=").append(esteNou);
        sb.append(", tipCombustibil='").append(tipCombustibil).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
