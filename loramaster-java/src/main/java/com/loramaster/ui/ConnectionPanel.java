package com.loramaster.ui;

import com.loramaster.communication.SocketManager;
import com.loramaster.communication.SocketListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.table.TableColumn;
import com.loramaster.ui.DataPanel;


/**
 * Панель управления подключением и сессиями.
 *
 * === Протокол взаимодействия с сервером ===
 *
 * 1. Получение списка сессий:
 *    - Клиент -> Сервер: "GET_MEASUREMENT_SESSIONS"
 *    - Сервер -> Клиент: "SESSIONS: [Session1ID, Session1, START_TIMESTAMP, LAST_TIMESTAMP, dotsCount]; [Session2ID, ...]; ...\n"
 *
 * 2. Начало измерения:
 *    - Клиент -> Сервер: "START_MEASUREMENT: <sessionID>"
 *    - Сервер -> Клиент: (в ответ сервер начинает присылать данные измерений)
 *
 * 3. Получение данных измерения:
 *    - Сервер -> Клиент: "MEASUREMENT: [Время, RSSI, SNR, Ошибки, Расстояние, Широта, Долгота]\n"
 *      (Сервер отправляет эти сообщения для всех исторических данных сессии, а затем для новых данных в реальном времени).
 *
 * 4. Добавление новой сессии:
 *    - Клиент -> Сервер: "ADD_SESSION: [sessionId, sessionName, startDate, lastDate, points]"
 *      (В текущей реализации sessionId генерируется на клиенте).
 *
 * 5. Удаление сессии:
 *    - Клиент -> Сервер: "REMOVE_SESSION: <sessionID>"
 *
 * Все сообщения от сервера должны заканчиваться символом новой строки ('\n').
 */

 
public class ConnectionPanel extends JPanel {
    private final JButton connectServerBtn;
    private final JLabel connectionIndicator;
    
    private final DefaultTableModel sessionsTableModel;
    private final JTable sessionsTable;

    private SocketManager socketManager;
    private DataPanel dataPanel;

    private final JButton addSessionBtn;
    private final JButton removeSessionBtn;

    public ConnectionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        connectServerBtn = new JButton("Подключиться к серверу");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(connectServerBtn, gbc);

        connectionIndicator = new JLabel("Статус: отключено");
        connectionIndicator.setForeground(Color.RED);
        gbc.gridx = 1;
        add(connectionIndicator, gbc);

        String[] columnNames = {"ID", "Сессия", "Дата начала", "Дата последнего измерения", "Количество точек"};
        sessionsTableModel = new DefaultTableModel(columnNames, 0);
        sessionsTable = new JTable(sessionsTableModel);
        TableColumn idColumn = sessionsTable.getColumnModel().getColumn(0);
        sessionsTable.removeColumn(idColumn);
        
        JScrollPane scrollPane = new JScrollPane(sessionsTable);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);

        JButton startMeasurementBtn = new JButton("Измерять");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(startMeasurementBtn, gbc);
        
        startMeasurementBtn.addActionListener(e -> {
            int viewRow = sessionsTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = sessionsTable.convertRowIndexToModel(viewRow);
                String sessionId = (String) sessionsTableModel.getValueAt(modelRow, 0);
                if (dataPanel != null) {
                    dataPanel.getTableModel().setRowCount(0);
                }
                socketManager.sendMessage("START_MEASUREMENT: " + sessionId);
            } else {
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Пожалуйста, выберите сессию", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        addSessionBtn = new JButton("Добавить сессию");
        removeSessionBtn = new JButton("Удалить сессию");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(addSessionBtn, gbc);
        gbc.gridx = 1;
        add(removeSessionBtn, gbc);
        
        addSessionBtn.addActionListener(e -> {
            String sessionName = JOptionPane.showInputDialog(ConnectionPanel.this, "Введите название сессии:");
            if (sessionName == null || sessionName.trim().isEmpty()) return;
            String startDate = JOptionPane.showInputDialog(ConnectionPanel.this, "Введите дату начала:");
            if (startDate == null || startDate.trim().isEmpty()) return;
            String lastDate = JOptionPane.showInputDialog(ConnectionPanel.this, "Введите дату последнего измерения:");
            if (lastDate == null || lastDate.trim().isEmpty()) return;
            String points = JOptionPane.showInputDialog(ConnectionPanel.this, "Введите количество точек:");
            if (points == null || points.trim().isEmpty()) return;
            String sessionId = "Session" + System.currentTimeMillis();
            String addCommand = "ADD_SESSION: [" + sessionId + ", " + sessionName + ", " + startDate + ", " + lastDate + ", " + points + "]"; 
            socketManager.sendMessage(addCommand);
        });
        
        removeSessionBtn.addActionListener(e -> {
            int viewRow = sessionsTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = sessionsTable.convertRowIndexToModel(viewRow);
                String sessionId = (String) sessionsTableModel.getValueAt(modelRow, 0);
                int confirm = JOptionPane.showConfirmDialog(ConnectionPanel.this, "Удалить сессию с ID: " + sessionId + "?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    socketManager.sendMessage("REMOVE_SESSION: " + sessionId);
                }
            } else {
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Пожалуйста, выберите сессию для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        connectServerBtn.addActionListener(e -> {
            new Thread(() -> {
                socketManager = new SocketManager();
                socketManager.addListener(new SocketListener() {
                    @Override
                    public void onMessage(String message) {
                        if (message.startsWith("SESSIONS:")) {
                            final String sessionsStr = message.substring("SESSIONS:".length()).trim();
                            SwingUtilities.invokeLater(() -> updateSessionsTable(sessionsStr));
                        } else if (message.startsWith("MEASUREMENT:")) {
                            final String measurementStr = message.substring("MEASUREMENT:".length()).trim();
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[(.*?)\\]");
                            java.util.regex.Matcher matcher = pattern.matcher(measurementStr);
                            if (matcher.find()) {
                                String entry = matcher.group(1);
                                String[] tokens = entry.split("\\s*,\\s*");
                                if (tokens.length == 7 && dataPanel != null) {
                                    SwingUtilities.invokeLater(() -> {
                                        dataPanel.addMeasurementRow(tokens);
                                    });
                                }
                            }
                        }
                    }
                    @Override
                    public void onDisconnect() {
                        SwingUtilities.invokeLater(() -> {
                            setConnectionStatus(false);
                            JOptionPane.showMessageDialog(ConnectionPanel.this,
                                "Сервер отключил соединение",
                                "Отключение",
                                JOptionPane.WARNING_MESSAGE);
                        });
                    }
                });
                boolean connected = socketManager.connect("localhost", 8080);
                SwingUtilities.invokeLater(() -> setConnectionStatus(connected));
                if (connected) {
                    socketManager.sendMessage("GET_MEASUREMENT_SESSIONS");
                }
            }).start();
        });
    }

    public JButton getConnectServerBtn() {
        return connectServerBtn;
    }

    public void setDataPanel(DataPanel dataPanel) {
        this.dataPanel = dataPanel;
    }

    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionIndicator.setText("Статус: подключено");
            connectionIndicator.setForeground(Color.GREEN.darker());
        } else {
            connectionIndicator.setText("Статус: отключено");
            connectionIndicator.setForeground(Color.RED);
        }
    }

    private void updateSessionsTable(String sessionsStr) {
        sessionsTableModel.setRowCount(0);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[(.*?)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(sessionsStr);
        while (matcher.find()) {
            String entry = matcher.group(1);
            String[] columns = entry.split("\\s*,\\s*");
            if (columns.length == 5) {
                String[] row = {columns[0], columns[1], columns[2], columns[3], columns[4]};
                sessionsTableModel.addRow(row);
            }
        }
    }
}
