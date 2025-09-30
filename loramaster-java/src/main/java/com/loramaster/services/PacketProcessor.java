package com.loramaster.services;

import com.loramaster.core.PacketInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class PacketProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PacketProcessor.class);

    public Optional<PacketInfo> parseLine(String line) {
        try {
            // пример строки: "RSSI=-120;SNR=7;ERR=0;DIST=1234;LAT=55.75;LON=37.61"
            String[] parts = line.split(";");
            int rssi = 0, snr = 0, distance = 0;
            double lat = 0, lon = 0;

            for (String p : parts) {
                String[] kv = p.split("=");
                if (kv.length != 2) continue;
                switch (kv[0].trim()) {
                    case "RSSI":
                        rssi = Integer.parseInt(kv[1]);
                        break;
                    case "SNR":
                        snr = Integer.parseInt(kv[1]);
                        break;
                    case "DIST":
                        distance = Integer.parseInt(kv[1]);
                        break;
                    case "LAT":
                        lat = Double.parseDouble(kv[1]);
                        break;
                    case "LON":
                        lon = Double.parseDouble(kv[1]);
                        break;
                }
            }

            PacketInfo packet = new PacketInfo();
            packet.setTimestamp(LocalDateTime.now().toString());
            packet.setRssi(rssi);
            packet.setSnr(snr);
            packet.setDistance(distance);
            packet.setLatitude(lat);
            packet.setLongitude(lon);
            return Optional.of(packet);

        } catch (Exception e) {
            logger.warn("Ошибка парсинга строки: {}", line, e);
            return Optional.empty();
        }
    }
}
