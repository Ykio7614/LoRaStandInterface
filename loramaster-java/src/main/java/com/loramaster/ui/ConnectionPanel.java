package com.loramaster.ui;

import com.loramaster.communication.SocketManager;
import com.loramaster.communication.SocketListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ConnectionPanel extends JPanel {
    private final JButton connectServerBtn;
    private final JLabel connectionIndicator;
    
    private final DefaultTableModel sessionsTableModel;
    private final JTable sessionsTable;

    private SocketManager socketManager;

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

        String[] columnNames = {"Сессия", "Дата начала", "Дата последнего измерения", "Количество точек"};
        sessionsTableModel = new DefaultTableModel(columnNames, 0);
        sessionsTable = new JTable(sessionsTableModel);
        JScrollPane scrollPane = new JScrollPane(sessionsTable);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);

        connectServerBtn.addActionListener(e -> {
            new Thread(() -> {
                socketManager = new SocketManager();
                socketManager.addListener(new SocketListener() {
                    @Override
                    public void onMessage(String message) {
                        // Строка с ссесиями пример: "SESSIONS: [Session1ID, Session1, START_TIMESTAMP, LAST_TIMESTAMP, dotsCount]; [Session2ID, Session2, START_TIMESTAMP, LAST_TIMESTAMP, dotsCount]; [Session3ID, Session2, START_TIMESTAMP, LAST_TIMESTAMP, dotsCount]\n" 
                        if (message.startsWith("SESSIONS:")) {
                            final String sessionsStr = message.substring("SESSIONS:".length()).trim();
                            SwingUtilities.invokeLater(() -> updateSessionsTable(sessionsStr));
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
                String[] row = {columns[1], columns[2], columns[3], columns[4]};
                sessionsTableModel.addRow(row);
            }
        }
    }
}
