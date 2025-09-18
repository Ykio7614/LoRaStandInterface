package com.loramaster.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final ConnectionPanel connectionPanel;
    private final DataPanel dataPanel;
    private final MapPanel mapPanel;

    public MainWindow() {
        super("LoRa Receiver Client");

        // Создаём вкладки
        JTabbedPane tabbedPane = new JTabbedPane();

        connectionPanel = new ConnectionPanel();
        dataPanel = new DataPanel();
        mapPanel = new MapPanel();

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
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
