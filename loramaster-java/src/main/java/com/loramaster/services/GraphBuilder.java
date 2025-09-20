package com.loramaster.services;

import com.loramaster.core.PacketInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class GraphBuilder {
    private final TimeSeries rssiSeries = new TimeSeries("RSSI");
    private final TimeSeries snrSeries = new TimeSeries("SNR");

    public void addPacket(PacketInfo packet) {
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(packet.getTimestamp());
        Millisecond time = new Millisecond(java.util.Date.from(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        rssiSeries.addOrUpdate(time, packet.getRssi());
        snrSeries.addOrUpdate(time, packet.getSnr());
    }

    public JFreeChart buildRssiChart() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(rssiSeries);
        return ChartFactory.createTimeSeriesChart("RSSI", "Время", "dBm", dataset);
    }

    public JFreeChart buildSnrChart() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(snrSeries);
        return ChartFactory.createTimeSeriesChart("SNR", "Время", "dB", dataset);
    }
}
