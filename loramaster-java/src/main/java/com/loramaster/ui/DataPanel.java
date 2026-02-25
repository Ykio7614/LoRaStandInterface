package com.loramaster.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DataPanel extends JPanel {
    private final JTable packetTable;
    private final JLabel selectedSessionLabel;
    private final JLabel currentSettingsLabel;
    private final List<Runnable> dataChangeListeners = new ArrayList<>(); // ✅ добавлено

    public DataPanel() {
        setLayout(new BorderLayout());

        selectedSessionLabel = new JLabel("Сессия не выбрана");
        add(selectedSessionLabel, BorderLayout.NORTH);

        // Таблица пакетов
        String[] columns = {"Время", "RSSI", "SNR", "hDop", "Широта", "Долгота"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        packetTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(packetTable);
        add(scrollPane, BorderLayout.CENTER);

        // Текущие настройки
        currentSettingsLabel = new JLabel("");//Настройки: SF=?, TX=?, BW=?
        add(currentSettingsLabel, BorderLayout.SOUTH);
    }

    public DefaultTableModel getTableModel() {
        return (DefaultTableModel) packetTable.getModel();
    }

    public void setCurrentSettings(String settingsText) {
        currentSettingsLabel.setText(settingsText);
    }

    public void setSelectedSessionName(String sessionName) {
        if (sessionName == null || sessionName.trim().isEmpty()) {
            selectedSessionLabel.setText("Сессия не выбрана");
        } else {
            selectedSessionLabel.setText("Выбранная сессия: " + sessionName);
        }
    }

    // ✅ Добавлено: механизм уведомления об изменениях
    public void addDataChangeListener(Runnable listener) {
        dataChangeListeners.add(listener);
    }

    private void notifyDataChanged() {
        for (Runnable listener : dataChangeListeners) {
            listener.run();
        }
    }

    public void addMeasurementRow(String[] rowData) {
        DefaultTableModel model = getTableModel();
        model.addRow(rowData);
        notifyDataChanged(); // ✅ уведомляем всех слушателей
    }

    /**
     * Получает все измерения из таблицы в формате JSON массива
     */
    public JSONArray getMeasurementsAsJson() throws JSONException {
        JSONArray measurements = new JSONArray();
        DefaultTableModel model = getTableModel();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            JSONObject measurement = new JSONObject();
            
            try {
                String time = model.getValueAt(i, 0).toString();
                measurement.put("datetime", time);

                String rssiStr = model.getValueAt(i, 1).toString();
                if (!rssiStr.isEmpty()) measurement.put("rssi", Double.parseDouble(rssiStr));

                String snrStr = model.getValueAt(i, 2).toString();
                if (!snrStr.isEmpty()) measurement.put("snr", Double.parseDouble(snrStr));

                String errorsStr = model.getValueAt(i, 3).toString();
                if (!errorsStr.isEmpty()) measurement.put("hDop", Float.parseFloat(errorsStr));

                

                String latStr = model.getValueAt(i, 4).toString();
                String lonStr = model.getValueAt(i, 5).toString();
                if (!latStr.isEmpty() && !lonStr.isEmpty()) {
                    measurement.put("latitude", Double.parseDouble(latStr));
                    measurement.put("longitude", Double.parseDouble(lonStr));
                    measurements.put(measurement);
                }
            } catch (NumberFormatException e) {
                System.err.println("Пропущена строка " + i + ": " + e.getMessage());
            }
        }
        
        return measurements;
    }

    public int getMeasurementsWithCoordsCount() {
        int count = 0;
        DefaultTableModel model = getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                String latStr = model.getValueAt(i, 4).toString();
                String lonStr = model.getValueAt(i, 5).toString();
                if (!latStr.isEmpty() && !lonStr.isEmpty()) {
                    Double.parseDouble(latStr);
                    Double.parseDouble(lonStr);
                    count++;
                }
            } catch (Exception ignored) {}
        }
        return count;
    }

    public int getTotalMeasurementsCount() {
        return getTableModel().getRowCount();
    }
}
