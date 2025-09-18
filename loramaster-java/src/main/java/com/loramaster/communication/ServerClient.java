package com.loramaster.communication;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.function.Consumer;

public class ServerClient {
    private static final Logger logger = LoggerFactory.getLogger(ServerClient.class);

    private Socket socket;

    public void connect(String url, Consumer<String> onMessage, Runnable onConnect, Runnable onDisconnect) {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            socket = IO.socket(url, options);

            socket.on(Socket.EVENT_CONNECT, args -> {
                logger.info("Connected to server: {}", url);
                onConnect.run();
            });

            socket.on(Socket.EVENT_DISCONNECT, args -> {
                logger.info("Disconnected from server");
                onDisconnect.run();
            });

            socket.on("message", args -> {
                if (args.length > 0 && args[0] instanceof String) {
                    onMessage.accept((String) args[0]);
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            logger.error("Invalid server URL", e);
        }
    }

    public void sendMessage(String event, Object data) {
        if (socket != null && socket.connected()) {
            socket.emit(event, data);
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.close();
        }
    }

}
