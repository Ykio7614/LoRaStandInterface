package com.loramaster.communication;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketManager {
    private ServerClient client;
    private final List<SocketListener> listeners = new CopyOnWriteArrayList<>();

    public boolean connect(String host, int port) {
        client = new ServerClient();
        boolean connected = client.connect(host, port);
        if (connected) {
            new Thread(() -> {
                while (true) {
                    String msg = client.receiveMessage();
                    if (msg == null) {
                        for (SocketListener listener : listeners) {
                            listener.onDisconnect();
                        }
                        break;
                    } else {
                        for (SocketListener listener : listeners) {
                            listener.onMessage(msg);
                        }
                    }
                }
            }).start();
        }
        return connected;
    }

    public boolean connect(String host, int port, int localPort) {
        client = new ServerClient();
        boolean connected = client.connect(host, port, localPort);
        if (connected) {
            new Thread(() -> {
                while (true) {
                    String msg = client.receiveMessage();
                    if (msg == null) {
                        for (SocketListener listener : listeners) {
                            listener.onDisconnect();
                        }
                        break;
                    } else {
                        for (SocketListener listener : listeners) {
                            listener.onMessage(msg);
                        }
                    }
                }
            }).start();
        }
        return connected;
    }

    public void sendMessage(String message) {
        if (client != null) {
            client.sendMessage(message);
        }
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }

    public void addListener(SocketListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SocketListener listener) {
        listeners.remove(listener);
    }
}
