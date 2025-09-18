package com.loramaster.ui;

import javax.swing.*;
import java.awt.*;

public class ConnectionPanel extends JPanel {
    private final JTextField serverUrlField;
    private final JButton connectServerBtn;
    private final JComboBox<String> portList;
    private final JButton connectSerialBtn;

    public ConnectionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Сервер
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("URL сервера:"), gbc);

        serverUrlField = new JTextField("http://localhost:5000");
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(serverUrlField, gbc);

        connectServerBtn = new JButton("Подключиться к серверу");
        gbc.gridx = 2; gbc.weightx = 0;
        add(connectServerBtn, gbc);

        // Serial
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("COM-порт:"), gbc);

        portList = new JComboBox<>(new String[]{"COM1", "COM2", "COM3"});
        gbc.gridx = 1;
        add(portList, gbc);

        connectSerialBtn = new JButton("Подключиться к Serial");
        gbc.gridx = 2;
        add(connectSerialBtn, gbc);
    }

    public String getServerUrl() {
        return serverUrlField.getText();
    }

    public JButton getConnectServerBtn() {
        return connectServerBtn;
    }

    public JComboBox<String> getPortList() {
        return portList;
    }

    public JButton getConnectSerialBtn() {
        return connectSerialBtn;
    }
}
