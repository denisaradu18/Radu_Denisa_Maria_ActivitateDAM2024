package com.example.invat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Obiect implements Parcelable {
    private String nume;
    private float pret;
    private long data;

    private String tip;

    public Obiect(String nume, float pret, long data, String tip) {
        this.nume = nume;
        this.pret = pret;
        this.data = data;
        this.tip = tip;
    }

    protected Obiect(Parcel in) {
        nume = in.readString();
        pret = in.readFloat();
        data = in.readInt();
        tip = in.readString();
    }

    public static final Creator<Obiect> CREATOR = new Creator<Obiect>() {
        @Override
        public Obiect createFromParcel(Parcel in) {
            return new Obiect(in);
        }

        @Override
        public Obiect[] newArray(int size) {
            return new Obiect[size];
        }
    };

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public float getPret() {
        return pret;
    }

    public void setPret(float pret) {
        this.pret = pret;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Obiect");
        sb.append("nume='").append(nume).append('\'');
        sb.append(", pret=").append(pret);
        sb.append(", data=").append(data);
        sb.append(", tip='").append(tip).append('\'');

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(nume);
        dest.writeFloat(pret);
        dest.writeLong(data);
        dest.writeString(tip);
    }
}
