package com.example.carte;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Carti")
public class Carte implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private int ISBN;
    private String titlu;

    private String gen;

    public Carte(int ISBN, String titlu, String gen) {
        this.ISBN = ISBN;
        this.titlu = titlu;
        this.gen = gen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    protected Carte(Parcel in) {
        ISBN = in.readInt();
        titlu = in.readString();
        gen = in.readString();
    }

    public static final Creator<Carte> CREATOR = new Creator<Carte>() {
        @Override
        public Carte createFromParcel(Parcel in) {
            return new Carte(in);
        }

        @Override
        public Carte[] newArray(int size) {
            return new Carte[size];
        }
    };

    public int getISBN() {
        return ISBN;
    }

    public void setISBN(int ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitlu() {
        return titlu;
    }

    public void setTitlu(String titlu) {
        this.titlu = titlu;
    }

    public String getGen() {
        return gen;
    }

    public void setGen(String gen) {
        this.gen = gen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(ISBN);
        dest.writeString(titlu);
        dest.writeString(gen);
    }
}
