package com.loramaster.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final ConnectionPanel connectionPanel;
    private final DataPanel dataPanel;
    private final MapPanel mapPanel;

    public MainWindow(int localPort) {
        super("LoRa Receiver Client");

        // Создаём вкладки
        JTabbedPane tabbedPane = new JTabbedPane();
        connectionPanel = new ConnectionPanel(localPort);
        dataPanel = new DataPanel();
        mapPanel = new MapPanel();

        connectionPanel.setDataPanel(dataPanel);

        tabbedPane.addTab("Подключение", connectionPanel);
        tabbedPane.addTab("Данные", dataPanel);
        tabbedPane.addTab("Карта", mapPanel);

        add(tabbedPane, BorderLayout.CENTER);

        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public ConnectionPanel getConnectionPanel() {
        return connectionPanel;
    }

    public DataPanel getDataPanel() {
        return dataPanel;
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public static void main(String[] args) {
        int localPort = 0; //автоматический выбор порта

        if (args.length > 0) {
            try {
                localPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number provided. Using default port 0.");
            }
        }

        final int finalLocalPort = localPort;
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow(finalLocalPort);
            window.setVisible(true);
        });
    }
}
