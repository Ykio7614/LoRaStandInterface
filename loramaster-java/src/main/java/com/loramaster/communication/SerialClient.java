package com.loramaster.communication;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Consumer;

public class SerialClient {
    private static final Logger logger = LoggerFactory.getLogger(SerialClient.class);

    private SerialPort serialPort;
    private Thread readerThread;

    public boolean connect(String portName, int baudRate, Consumer<String> onLine) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);

        if (!serialPort.openPort()) {
            logger.error("Failed to open serial port {}", portName);
            return false;
        }

        logger.info("Serial port {} opened", portName);

        readerThread = new Thread(() -> {
            try (InputStream inputStream = serialPort.getInputStream();
                 Scanner scanner = new Scanner(inputStream)) {
                while (!Thread.currentThread().isInterrupted() && scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    onLine.accept(line);
                }
            } catch (Exception e) {
                logger.error("Error reading from serial port", e);
            }
        });
        readerThread.start();
        return true;
    }

    public void disconnect() {
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            logger.info("Serial port closed");
        }
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
}
