package com.loramaster.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerClient {
    private static final Logger logger = LoggerFactory.getLogger(ServerClient.class);

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public boolean connect(String host, int port, int localPort) {
        try {
            socket = new Socket(host, port, java.net.InetAddress.getLocalHost(), localPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            logger.info("Connected to {}:{}", host, port);
            return true;
        } catch (UnknownHostException e) {
            logger.error("Unknown host: {}", host, e);
            return false;
        } catch (IOException e) {
            logger.error("I/O error when connecting to {}:{}", host, port, e);
            return false;
        }
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            logger.info("Connected to {}:{}", host, port);
            return true;
        } catch (UnknownHostException e) {
            logger.error("Unknown host: {}", host, e);
            return false;
        } catch (IOException e) {
            logger.error("I/O error when connecting to {}:{}", host, port, e);
            return false;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            logger.info("Sent message: {}", message);
        } else {
            logger.warn("Output stream is not initialized.");
        }
    }

    public String receiveMessage() {
        if (in != null) {
            try {
                String message = in.readLine();
                logger.info("Received message: {}", message);
                return message;
            } catch (IOException e) {
                logger.error("Error reading message", e);
            }
        } else {
            logger.warn("Input stream is not initialized.");
        }
        return null;
    }

    public void disconnect() {
        try {
            if(socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Disconnected from server.");
            }
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }
    }
}
