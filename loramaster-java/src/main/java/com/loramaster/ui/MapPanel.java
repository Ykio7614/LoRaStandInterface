package com.loramaster.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MapPanel extends JPanel {
    private final JButton openMapBtn;

    public MapPanel() {
        setLayout(new FlowLayout());
        openMapBtn = new JButton("Открыть карту в браузере");
        add(openMapBtn);

        openMapBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new File("map.html").toURI());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Не удалось открыть карту: " + ex.getMessage());
            }
        });
    }
}
