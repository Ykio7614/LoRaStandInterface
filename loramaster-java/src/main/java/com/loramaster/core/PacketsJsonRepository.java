package com.loramaster.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PacketsJsonRepository {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path filePath;
    private List<PacketInfo> packets = new ArrayList<>();

    public PacketsJsonRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        if (Files.exists(filePath)) {
            try (FileReader reader = new FileReader(filePath.toFile())) {
                Type listType = new TypeToken<List<PacketInfo>>(){}.getType();
                packets = gson.fromJson(reader, listType);
                if (packets == null) {
                    packets = new ArrayList<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save() {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(packets, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<PacketInfo> getPackets() {
        return packets;
    }

    public void addPacket(PacketInfo packet) {
        packets.add(packet);
        save();
    }

    public void reset(List<PacketInfo> newPackets) {
        this.packets = new ArrayList<>(newPackets);
        save();
    }
}
