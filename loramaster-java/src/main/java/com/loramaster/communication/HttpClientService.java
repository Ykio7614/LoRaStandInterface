package com.loramaster.communication;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientService {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);

    private final String baseUrl;

    public HttpClientService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean sendJson(String endpoint, String json) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(baseUrl + endpoint);
            request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/json");

            client.execute(request, response -> {
                int code = response.getCode();
                logger.info("POST {} returned {}", endpoint, code);
                return code;
            });

            return true;
        } catch (IOException e) {
            logger.error("Failed to send HTTP request", e);
            return false;
        }
    }
}
