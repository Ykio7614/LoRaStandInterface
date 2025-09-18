package com.loramaster.core;

public class Settings {
    private int sf;
    private int tx;
    private double bw;


    public Settings() {}

    public Settings(int sf, int tx, double bw, Double latitude, Double longitude) {
        this.sf = sf;
        this.tx = tx;
        this.bw = bw;

    }

    public int getSf() { return sf; }
    public void setSf(int sf) { this.sf = sf; }

    public int getTx() { return tx; }
    public void setTx(int tx) { this.tx = tx; }

    public double getBw() { return bw; }
    public void setBw(double bw) { this.bw = bw; }


}
