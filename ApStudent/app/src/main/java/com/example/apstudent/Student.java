package com.example.apstudent;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Student implements Parcelable {
    private String nume;
    private String sex;
    private int an;
    private String curs;
    private float rating;

    public Student(String nume, String sex, int an, String curs, float rating) {
        this.nume = nume;
        this.sex = sex;
        this.an = an;
        this.curs = curs;
        this.rating = rating;
    }

    protected Student(Parcel in) {
        nume = in.readString();
        sex = in.readString();
        an = in.readInt();
        curs = in.readString();
        rating = in.readFloat();
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAn() {
        return an;
    }

    public void setAn(int an) {
        this.an = an;
    }

    public String getCurs() {
        return curs;
    }

    public void setCurs(String curs) {
        this.curs = curs;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Student{");
        sb.append("nume='").append(nume).append('\'');
        sb.append(", sex='").append(sex).append('\'');
        sb.append(", an=").append(an);
        sb.append(", curs='").append(curs).append('\'');
        sb.append(", rating=").append(rating);
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
        dest.writeString(sex);
        dest.writeInt(an);
        dest.writeString(curs);
        dest.writeFloat(rating);
    }
}
