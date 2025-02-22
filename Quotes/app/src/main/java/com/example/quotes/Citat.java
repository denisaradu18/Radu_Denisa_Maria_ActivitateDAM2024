package com.example.quotes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "citat")
public class Citat implements Parcelable {
    @PrimaryKey
    @NonNull

    private String text;
    private String autor;
    private String gen;

    public Citat(String text, String autor, String gen) {
        this.text = text;
        this.autor = autor;
        this.gen = gen;
    }

    protected Citat(Parcel in) {
        text = in.readString();
        autor = in.readString();
        gen = in.readString();
    }

    public static final Creator<Citat> CREATOR = new Creator<Citat>() {
        @Override
        public Citat createFromParcel(Parcel in) {
            return new Citat(in);
        }

        @Override
        public Citat[] newArray(int size) {
            return new Citat[size];
        }
    };

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Citat{");
        sb.append("text='").append(text).append('\'');
        sb.append(", autor='").append(autor).append('\'');
        sb.append(", gen='").append(gen).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
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

public String getKey()
{
    StringBuilder sb=new StringBuilder();
    sb.append(this.autor);
    sb.append("-");
    sb.append(this.text);

    return sb.toString();
}

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(autor);
        dest.writeString(gen);
    }
}
