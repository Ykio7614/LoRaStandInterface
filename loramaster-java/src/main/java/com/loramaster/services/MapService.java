package com.loramaster.services;

import com.loramaster.core.PacketInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapService {
    private static final Logger logger = LoggerFactory.getLogger(MapService.class);

    private final List<PacketInfo> packets = new ArrayList<>();

    public void addPacket(PacketInfo packet) {
        if (packet.getLatitude() != 0 && packet.getLongitude() != 0) {
            packets.add(packet);
        }
    }

    public void generateHtmlMap(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(
                    "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "  <meta charset=\"UTF-8\">\n" +
                    "  <title>LoRa Map</title>\n" +
                    "  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet/dist/leaflet.css\"/>\n" +
                    "  <script src=\"https://unpkg.com/leaflet/dist/leaflet.js\"></script>\n" +
                    "  <style>#map { height: 100vh; }</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "  <div id=\"map\"></div>\n" +
                    "  <script>\n" +
                    "    var map = L.map('map').setView([55.75, 37.61], 12);\n" +
                    "    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                    "      attribution: '© OpenStreetMap contributors'\n" +
                    "    }).addTo(map);\n"
            );

            for (PacketInfo p : packets) {
                writer.write(String.format(
                        "L.marker([%f, %f]).addTo(map).bindPopup('RSSI=%d SNR=%d');\n",
                        p.getLatitude(), p.getLongitude(), p.getRssi(), p.getSnr()
                ));
            }

            writer.write(
                    "  </script>\n" +
                    "</body>\n" +
                    "</html>\n"
            );

            logger.info("Карта сгенерирована: {}", filePath);

        } catch (IOException e) {
            logger.error("Ошибка генерации карты", e);
        }
    }
}
