package com.example.recapitulam;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Cursa")
public class Cursa {
    @PrimaryKey
    @NonNull
    private int id;
    private String destinatie;
    private int distamta;
    private Boolean esteManuala;

    public int getId() {
        return id;
    }

    @Override
    public String
    toString() {
        final StringBuilder sb = new StringBuilder("Cursa{");
        sb.append("id=").append(id);
        sb.append(", destinatie='").append(destinatie).append('\'');
        sb.append(", distamta=").append(distamta);
        sb.append(", esteManuala=").append(esteManuala);
        sb.append('}');
        return sb.toString();
    }

    public Cursa(int id, String destinatie, int distamta, Boolean esteManuala) {
        this.id = id;
        this.destinatie = destinatie;
        this.distamta = distamta;
        this.esteManuala = esteManuala;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDestinatie() {
        return destinatie;
    }

    public void setDestinatie(String destinatie) {
        this.destinatie = destinatie;
    }

    public int getDistamta() {
        return distamta;
    }

    public void setDistamta(int distamta) {
        this.distamta = distamta;
    }

    public Boolean getEsteManuala() {
        return esteManuala;
    }

    public void setEsteManuala(Boolean esteManuala) {
        this.esteManuala = esteManuala;
    }
}
