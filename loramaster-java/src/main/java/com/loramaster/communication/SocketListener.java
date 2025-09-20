package com.loramaster.communication;

public interface SocketListener {
    void onMessage(String message);
    void onDisconnect();
}
