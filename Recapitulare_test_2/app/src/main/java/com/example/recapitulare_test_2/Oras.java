package com.example.recapitulare_test_2;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "oras")
public class Oras implements Parcelable {
    @PrimaryKey()
    @NonNull
    private String denumire;
    private int numarLocuitori;
    private String judet;

    public Oras(String denumire, int numarLocuitori, String judet) {
        this.denumire = denumire;
        this.numarLocuitori = numarLocuitori;
        this.judet = judet;
    }

    protected Oras(Parcel in) {
        denumire = in.readString();
        numarLocuitori = in.readInt();
        judet = in.readString();
    }

    public static final Creator<Oras> CREATOR = new Creator<Oras>() {
        @Override
        public Oras createFromParcel(Parcel in) {
            return new Oras(in);
        }

        @Override
        public Oras[] newArray(int size) {
            return new Oras[size];
        }
    };

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }

    public int getNumarLocuitori() {
        return numarLocuitori;
    }

    public void setNumarLocuitori(int numarLocuitori) {
        this.numarLocuitori = numarLocuitori;
    }

    public String getJudet() {
        return judet;
    }

    public void setJudet(String judet) {
        this.judet = judet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(denumire);
        dest.writeInt(numarLocuitori);
        dest.writeString(judet);
    }
}
