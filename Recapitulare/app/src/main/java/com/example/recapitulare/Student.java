package com.example.recapitulare;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Student implements Parcelable {
    private String nume;
    private double medie;
    private int anStudiu;
    private boolean esteIntegralist;
    private String niveStudiu;

    public Student(String nume, double medie, int anStudiu, boolean esteIntegralist, String niveStudiu) {
        this.nume = nume;
        this.medie = medie;
        this.anStudiu = anStudiu;
        this.esteIntegralist = esteIntegralist;
        this.niveStudiu = niveStudiu;
    }

    protected Student(Parcel in) {
        nume = in.readString();
        medie = in.readDouble();
        anStudiu = in.readInt();
        esteIntegralist = in.readByte() != 0;
        niveStudiu = in.readString();
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public double getMedie() {
        return medie;
    }

    public void setMedie(double medie) {
        this.medie = medie;
    }

    public int getAnStudiu() {
        return anStudiu;
    }

    public void setAnStudiu(int anStudiu) {
        this.anStudiu = anStudiu;
    }

    public boolean isEsteIntegralist() {
        return esteIntegralist;
    }

    public void setEsteIntegralist(boolean esteIntegralist) {
        this.esteIntegralist = esteIntegralist;
    }

    public String getNiveStudiu() {
        return niveStudiu;
    }

    public void setNiveStudiu(String niveStudiu) {
        this.niveStudiu = niveStudiu;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Student{");
        sb.append("nume='").append(nume).append('\'');
        sb.append(", medie=").append(medie);
        sb.append(", anStudiu=").append(anStudiu);
        sb.append(", esteIntegralist=").append(esteIntegralist);
        sb.append(", niveStudiu='").append(niveStudiu).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(nume);
        dest.writeDouble(medie);
        dest.writeInt(anStudiu);
        dest.writeByte((byte) (esteIntegralist ? 1 : 0));
        dest.writeString(niveStudiu);
    }

}
