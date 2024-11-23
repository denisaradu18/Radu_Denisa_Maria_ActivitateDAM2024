package com.example.recap;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Calculator implements Parcelable {
    private String denumire;
    private int memorie;
    private String tipPtocesor;
    private boolean windowsInstalat;

    private String model;

    public Calculator(String denumire, int memorie, String tipPtocesor, boolean windowsInstalat) {
        this.denumire = denumire;
        this.memorie = memorie;
        this.tipPtocesor = tipPtocesor;
        this.windowsInstalat = windowsInstalat;
    }

    public Calculator(String denumire, int memorie, String tipPtocesor, boolean windowsInstalat, String model) {
        this.denumire = denumire;
        this.memorie = memorie;
        this.tipPtocesor = tipPtocesor;
        this.windowsInstalat = windowsInstalat;
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    protected Calculator(Parcel in) {
        denumire = in.readString();
        memorie = in.readInt();
        tipPtocesor = in.readString();
        windowsInstalat = in.readByte() != 0;
    }

    public static final Creator<Calculator> CREATOR = new Creator<Calculator>() {
        @Override
        public Calculator createFromParcel(Parcel in) {
            return new Calculator(in);
        }

        @Override
        public Calculator[] newArray(int size) {
            return new Calculator[size];
        }
    };

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }

    public int getMemorie() {
        return memorie;
    }

    public void setMemorie(int memorie) {
        this.memorie = memorie;
    }

    public String getTipPtocesor() {
        return tipPtocesor;
    }

    public void setTipPtocesor(String tipPtocesor) {
        this.tipPtocesor = tipPtocesor;
    }

    public boolean isWindowsInstalat() {
        return windowsInstalat;
    }

    public void setWindowsInstalat(boolean windowsInstalat) {
        this.windowsInstalat = windowsInstalat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Calculator{");
        sb.append("denumire='").append(denumire).append('\'');
        sb.append(", memorie=").append(memorie);
        sb.append(", tipPtocesor='").append(tipPtocesor).append('\'');
        sb.append(", windowsInstalat=").append(windowsInstalat);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(denumire);
        dest.writeInt(memorie);
        dest.writeString(tipPtocesor);
        dest.writeByte((byte) (windowsInstalat ? 1 : 0));
    }
}
