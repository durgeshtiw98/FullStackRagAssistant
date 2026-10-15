package com.example.rag.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class HttpEmbeddingClientTest {

    @Test
    void sendsJsonWithoutHttp2UpgradeToHttp11EmbeddingService() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> upgrade = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        server.createContext("/embed", exchange -> {
            upgrade.set(exchange.getRequestHeaders().getFirst("Upgrade"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"embeddings\":[[0.6,0.8]]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        try {
            var client = new HttpEmbeddingClient("http://127.0.0.1:" + server.getAddress().getPort());
            var vectors = client.embed(List.of("Apollo 11"));
            assertNull(upgrade.get(), "Uvicorn must not receive an h2c upgrade request");
            assertEquals("Apollo 11", new ObjectMapper().readTree(requestBody.get()).get("texts").get(0).asText());
            assertEquals(1, vectors.size());
            assertArrayEquals(new float[]{0.6f, 0.8f}, vectors.get(0), 0.00001f);
        } finally {
            server.stop(0);
        }
    }
}
