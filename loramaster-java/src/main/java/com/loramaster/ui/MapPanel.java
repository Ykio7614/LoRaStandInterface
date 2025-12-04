package com.loramaster.ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONArray;

public class MapPanel extends JPanel {
    private JFXPanel jfxPanel;
    private WebEngine webEngine;
    private JButton generateButton;
    private DataPanel dataPanel;

    private static final String JSON_DIRECTORY = "generated_jsons";
    private static final String MAP_DIRECTORY = "generated_maps";

    public MapPanel(DataPanel dataPanel) {
        this.dataPanel = dataPanel;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Панель с кнопкой
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateButton = new JButton("Сгенерировать карту");
        controlPanel.add(generateButton);
        add(controlPanel, BorderLayout.NORTH);

        // Создаем слой для webview + полоски
        JLayeredPane layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // JFXPanel с WebView
        jfxPanel = new JFXPanel();
        jfxPanel.setBounds(0, 0, 800, 600); // ширина/высота будут обновляться позже
        layeredPane.add(jfxPanel, JLayeredPane.DEFAULT_LAYER);

        // Полоска снизу поверх webview
        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBounds(0, 580, 800, 20); // 20px высота, позиция снизу
        bottomBar.setOpaque(true);
        layeredPane.add(bottomBar, JLayeredPane.PALETTE_LAYER); // выше DEFAULT_LAYER

        // Обновляем размеры при изменении панели
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int h = getHeight() - controlPanel.getHeight();
                jfxPanel.setBounds(0, 0, w, h);
                bottomBar.setBounds(0, h - 20, w, 20);
            }
        });

        Platform.runLater(this::initializeWebView);

        generateButton.addActionListener(e -> generateMap());
    }

    private void initializeWebView() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.loadContent(
            "<html><body style='display:flex;justify-content:center;align-items:center;height:100vh;margin:0;'>" +
            "<div style='text-align:center;color:#666;'>" +
            "<h2>Карта не загружена</h2>" +
            "<p>JSON будет сохранён в директории: " + JSON_DIRECTORY + "</p>" +
            "<p>HTML карта будет сохранена в директории: " + MAP_DIRECTORY + "</p>" +
            "</div></body></html>"
        );

        Scene scene = new Scene(webView);
        jfxPanel.setScene(scene);
    }

    private void generateMap() {
    if (dataPanel == null) {
        JOptionPane.showMessageDialog(this, "DataPanel не подключена", "Ошибка", JOptionPane.ERROR_MESSAGE);
        return;
    }

    new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {
            // Используем final для переменных, чтобы их можно было использовать в лямбде
            final File jsonDir = new File(JSON_DIRECTORY);
            final File mapDir = new File(MAP_DIRECTORY);

            try {
                if (!jsonDir.exists()) jsonDir.mkdirs();
                if (!mapDir.exists()) mapDir.mkdirs();

                final File jsonFile = new File(jsonDir, "measurements.json");
                JSONArray measurements = dataPanel.getMeasurementsAsJson();
                try (FileWriter writer = new FileWriter(jsonFile)) {
                    writer.write(measurements.toString(2));
                }
                System.out.println("[DEBUG] JSON сохранён: " + jsonFile.getAbsolutePath());

                final File htmlFile = new File(mapDir, "map.html");

                // Запуск Python скрипта
                String pythonPath = "python"; // или полный путь к python.exe
                File jarDir = new File(MapPanel.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI())
                        .getParentFile();

                File pythonScript = new File(jarDir, "map_generator.py");
                if (!pythonScript.exists()) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(MapPanel.this,
                                    "Скрипт map_generator.py не найден в " + jarDir,
                                    "Ошибка", JOptionPane.ERROR_MESSAGE));
                    return null;
                }

                ProcessBuilder pb = new ProcessBuilder(
                        pythonPath,
                        pythonScript.getAbsolutePath(),
                        jsonFile.getAbsolutePath(),
                        htmlFile.getAbsolutePath()
                );
                pb.directory(jarDir);
                pb.redirectErrorStream(true);

                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON] " + line);
                    }
                }
                int exitCode = process.waitFor();
                System.out.println("[DEBUG] Python завершился с кодом: " + exitCode);

                if (exitCode == 0 && htmlFile.exists()) {
                    Platform.runLater(() -> {
                        String url = "file:///" + htmlFile.getAbsolutePath().replace("\\", "/");
                        webEngine.load(url);

                        // Удаляем папки после загрузки
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                deleteDirectoryRecursively(jsonDir);
                                deleteDirectoryRecursively(mapDir);
                                System.out.println("[DEBUG] Временные папки удалены");
                            } catch (InterruptedException ignored) {}
                        }).start();
                    });
                } else {
                    Platform.runLater(() -> {
                        webEngine.loadContent("<h2 style='color:red'>Ошибка генерации карты</h2>");
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    webEngine.loadContent("<h2 style='color:red'>Исключение: " + ex.getMessage() + "</h2>");
                });
            }
            return null;
        }
    }.execute();
}


    private void deleteDirectoryRecursively(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    public void setDataPanel(DataPanel dataPanel) {
        this.dataPanel = dataPanel;
    }
}
