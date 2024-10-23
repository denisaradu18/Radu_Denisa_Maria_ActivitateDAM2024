package com.example.seminar4;

public class Masina {
   private String model;
   private int anFabricatie;
    private float km;
    private float pret;
    private String marca;
    private boolean esteNou;

    public Masina(String model, int anFabricatie, float km, float pret, String marca) {
        this.model = model;
        this.anFabricatie = anFabricatie;
        this.km = km;
        this.pret = pret;
        this.marca = marca;
    }

    public String getModel() {
        return model;
    }

    public int getAnFabricatie() {
        return anFabricatie;
    }

    public float getKm() {
        return km;
    }

    public float getPret() {
        return pret;
    }

    public String getMarca() {
        return marca;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setAnFabricatie(int anFabricatie) {
        this.anFabricatie = anFabricatie;
    }

    public void setKm(float km) {
        this.km = km;
    }

    public void setPret(float pret) {
        this.pret = pret;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Masina{");
        sb.append("model='").append(model).append('\'');
        sb.append(", anFabricatie=").append(anFabricatie);
        sb.append(", km=").append(km);
        sb.append(", pret=").append(pret);
        sb.append(", marca='").append(marca).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
