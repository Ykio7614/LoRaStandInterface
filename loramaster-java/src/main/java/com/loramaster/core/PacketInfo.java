package com.loramaster.core;

public class PacketInfo {
    private double rssi; 
    private double snr;
    private double distance; // расстояние до источника сигнала в метрах
    private Double latitude;  // широта
    private Double longitude; // долгота
    private String timestamp; // временная метка получения пакета

    public PacketInfo() {}

    public PacketInfo(double rssi, double snr, int bitErrors, double distance,
                      Double latitude, Double longitude, String timestamp) {
        this.rssi = rssi;
        this.snr = snr;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public double getRssi() { return rssi; }
    public void setRssi(double rssi) { this.rssi = rssi; }

    public double getSnr() { return snr; }
    public void setSnr(double snr) { this.snr = snr; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
