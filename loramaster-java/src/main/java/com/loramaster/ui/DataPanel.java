package com.loramaster.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DataPanel extends JPanel {
    private final JTable packetTable;
    private final JLabel currentSettingsLabel;

    public DataPanel() {
        setLayout(new BorderLayout());

        // Таблица пакетов
        String[] columns = {"Время", "RSSI", "SNR", "Ошибки", "Расстояние", "Широта", "Долгота"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        packetTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(packetTable);
        add(scrollPane, BorderLayout.CENTER);

        // Текущие настройки
        currentSettingsLabel = new JLabel("Настройки: SF=?, TX=?, BW=?");
        add(currentSettingsLabel, BorderLayout.SOUTH);
    }

    public DefaultTableModel getTableModel() {
        return (DefaultTableModel) packetTable.getModel();
    }

    public void setCurrentSettings(String settingsText) {
        currentSettingsLabel.setText(settingsText);
    }
}
