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
    private final JButton startMeasurementBtn;
    private final JLabel connectionIndicator;
    private final JLabel masterIndicator;
    private final JLabel slaverIndicator;
    private final DefaultTableModel sessionsTableModel;
    private final JTable sessionsTable;
    private final JButton addSessionBtn;
    private final JButton removeSessionBtn;

    private final JLabel sfLabel;
    private final JTextField sfField;
    private final JLabel txLabel;
    private final JTextField txField;
    private final JLabel bwLabel;
    private final JTextField bwField;
    private final JButton setSettingsBtn;

    private SocketManager socketManager;
    private DataPanel dataPanel;
    private int localPort;

    public ConnectionPanel(int localPort) {
        this.localPort = localPort;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // ---------- Верхняя панель ----------
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        connectServerBtn = new JButton("Подключиться к серверу");
        leftTopPanel.add(connectServerBtn);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder("Статусы"));
        statusPanel.setLayout(new GridLayout(3, 1, 0, 5));

        JPanel serverStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        serverStatusPanel.add(new JLabel("Сервер:"));
        connectionIndicator = new JLabel("○ отключено");
        connectionIndicator.setForeground(Color.RED);
        serverStatusPanel.add(connectionIndicator);
        statusPanel.add(serverStatusPanel);

        JPanel masterStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        masterStatusPanel.add(new JLabel("Master:"));
        masterIndicator = new JLabel("○ отключено");
        masterIndicator.setForeground(Color.RED);
        masterStatusPanel.add(masterIndicator);
        statusPanel.add(masterStatusPanel);

        JPanel slaverStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        slaverStatusPanel.add(new JLabel("Slaver:"));
        slaverIndicator = new JLabel("○ отключено");
        slaverIndicator.setForeground(Color.RED);
        slaverStatusPanel.add(slaverIndicator);
        statusPanel.add(slaverStatusPanel);

        leftTopPanel.add(statusPanel);

        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        addSessionBtn = new JButton("Добавить сессию");
        removeSessionBtn = new JButton("Удалить сессию");
        rightTopPanel.add(addSessionBtn);
        rightTopPanel.add(removeSessionBtn);

        topPanel.add(leftTopPanel, BorderLayout.WEST);
        topPanel.add(rightTopPanel, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(topPanel, gbc);

        // ---------- Таблица ----------
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        String[] columnNames = {"ID", "Сессия", "Дата начала", "Дата последнего измерения", "Количество точек"};
        sessionsTableModel = new DefaultTableModel(columnNames, 0);
        sessionsTable = new JTable(sessionsTableModel);
        TableColumn idColumn = sessionsTable.getColumnModel().getColumn(0);
        sessionsTable.removeColumn(idColumn);
        JScrollPane scrollPane = new JScrollPane(sessionsTable);
        add(scrollPane, gbc);

        // ---------- Нижняя панель ----------
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sfLabel = new JLabel("SF:");
        leftBottomPanel.add(sfLabel);
        sfField = new JTextField(3);
        leftBottomPanel.add(sfField);

        txLabel = new JLabel("TX:");
        leftBottomPanel.add(txLabel);
        txField = new JTextField(3);
        leftBottomPanel.add(txField);

        bwLabel = new JLabel("BW:");
        leftBottomPanel.add(bwLabel);
        bwField = new JTextField(4);
        leftBottomPanel.add(bwField);

        setSettingsBtn = new JButton("Применить");
        leftBottomPanel.add(setSettingsBtn);

        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        startMeasurementBtn = new JButton("Измерять");
        rightBottomPanel.add(startMeasurementBtn);

        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightBottomPanel, BorderLayout.EAST);

        add(bottomPanel, gbc);

        connectServerBtn.addActionListener(e -> {
            if ("Подключиться к серверу".equals(connectServerBtn.getText())) {
                new Thread(() -> {
                    socketManager = new SocketManager();
                    socketManager.addListener(new SocketListener() {
                        @Override
                        public void onMessage(String message) {
                            if (message.startsWith("SESSIONS:")) {
                                final String sessionsStr = message.substring("SESSIONS:".length()).trim();
                                SwingUtilities.invokeLater(() -> updateSessionsTable(sessionsStr));
                            } else if (message.startsWith("MASTER_STATUS")) {
                                String status = message.substring("MASTER_STATUS".length()).trim();
                                SwingUtilities.invokeLater(() -> updateMasterStatus(status));
                            } else if (message.startsWith("SLAVER_STATUS")) {
                                String status = message.substring("SLAVER_STATUS".length()).trim();
                                SwingUtilities.invokeLater(() -> updateSlaverStatus(status));
                            } else if (message.startsWith("MEASUREMENT:")) {
                                final String measurementStr = message.substring("MEASUREMENT:".length()).trim();
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[(.*?)\\]");
                                java.util.regex.Matcher matcher = pattern.matcher(measurementStr);
                                if (matcher.find()) {
                                    String entry = matcher.group(1);
                                    String[] tokens = entry.split("\\s*,\\s*");
                                    if (tokens.length == 6 && dataPanel != null) {
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
                    boolean connected;
                    if (localPort > 0) {
                        connected = socketManager.connect("localhost", 8082, localPort);
                    } else {
                        connected = socketManager.connect("localhost", 8082);
                    }

                    SwingUtilities.invokeLater(() -> setConnectionStatus(connected));
                    if (connected) {
                        socketManager.sendMessage("GET_MEASUREMENT_SESSIONS");
                    }
                }).start();
            } else {
                if (socketManager != null) {
                    socketManager.disconnect();
                    setConnectionStatus(false);
                }
            }
        });

        startMeasurementBtn.addActionListener(e -> {
            int viewRow = sessionsTable.getSelectedRow();
            if (viewRow >= 0) {
                if (dataPanel != null) {
                    dataPanel.getTableModel().setRowCount(0);
                }
                int modelRow = sessionsTable.convertRowIndexToModel(viewRow);
                String sessionId = (String) sessionsTableModel.getValueAt(modelRow, 0);
                socketManager.sendMessage("START_MEASUREMENT: " + sessionId);
            } else {
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Пожалуйста, выберите сессию", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        addSessionBtn.addActionListener(e -> {
            String sessionName = JOptionPane.showInputDialog(ConnectionPanel.this, "Введите название сессии:");
            if (sessionName == null || sessionName.trim().isEmpty()) return;

            String today = java.time.LocalDateTime.now().toString();
            String points = "0";
            String sessionId = String.valueOf(System.currentTimeMillis());

            String addCommand = "ADD_SESSION: [" 
                    + sessionId + ", " 
                    + sessionName + ", " 
                    + today + ", " 
                    + today + ", " 
                    + points + "]";

            socketManager.sendMessage(addCommand);

            socketManager.sendMessage("GET_MEASUREMENT_SESSIONS");
        });


        removeSessionBtn.addActionListener(e -> {
            int viewRow = sessionsTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = sessionsTable.convertRowIndexToModel(viewRow);
                String sessionId = (String) sessionsTableModel.getValueAt(modelRow, 0);
                String sessionName = (String) sessionsTableModel.getValueAt(modelRow, 1);
                int confirm = JOptionPane.showConfirmDialog(ConnectionPanel.this,
                        "Удалить сессию с именем: " + sessionName + "?",
                        "Подтверждение удаления",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    socketManager.sendMessage("REMOVE_SESSION: " + sessionId);

                    socketManager.sendMessage("GET_MEASUREMENT_SESSIONS");
                }
            } else {
                JOptionPane.showMessageDialog(ConnectionPanel.this,
                        "Пожалуйста, выберите сессию для удаления",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        

        setSettingsBtn.addActionListener(e -> {
            String sf = sfField.getText();
            String tx = txField.getText();
            String bw = bwField.getText();

            if (sf.trim().isEmpty() || tx.trim().isEmpty() || bw.trim().isEmpty()) {
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Все поля настроек должны быть заполнены", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (socketManager != null) {
                String settingsCommand = "SET_SETTINGS: SF=" + sf + ", TX=" + tx + ", BW=" + bw;
                socketManager.sendMessage(settingsCommand);
                if (dataPanel != null) {
                    dataPanel.setCurrentSettings("Настройки: SF=" + sf + ", TX=" + tx + ", BW=" + bw);
                }
            } else {
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Сначала подключитесь к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
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
            connectionIndicator.setText("● подключено");
            connectionIndicator.setForeground(Color.GREEN.darker());
            connectServerBtn.setText("Отключиться от сервера");
        } else {
            connectionIndicator.setText("○ отключено");
            connectionIndicator.setForeground(Color.RED);
            connectServerBtn.setText("Подключиться к серверу");
        }
    }

    private void updateMasterStatus(String status) {
        if ("CONNECTED".equalsIgnoreCase(status)) {
            masterIndicator.setText("● подключено");
            masterIndicator.setForeground(Color.GREEN.darker());
        } else {
            masterIndicator.setText("○ отключено");
            masterIndicator.setForeground(Color.RED);
        }
    }

    private void updateSlaverStatus(String status) {
        if ("CONNECTED".equalsIgnoreCase(status)) {
            slaverIndicator.setText("● подключено");
            slaverIndicator.setForeground(Color.GREEN.darker());
        } else {
            slaverIndicator.setText("○ отключено");
            slaverIndicator.setForeground(Color.RED);
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
                sessionsTableModel.addRow(columns);
            }
        }
    }
}