package com.example.rag.client;

import com.example.rag.core.EmbeddingClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Calls the ONNX embedding service: POST {baseUrl}/embed with {"texts": [...]}. */
public final class HttpEmbeddingClient implements EmbeddingClient {

    // Uvicorn serves HTTP/1.1; an h2c upgrade can cause it to lose the request body.
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI endpoint;

    public HttpEmbeddingClient(String baseUrl) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.endpoint = URI.create(trimmed + "/embed");
    }

    @Override
    public List<float[]> embed(List<String> texts) throws IOException {
        String body = mapper.writeValueAsString(Map.of("texts", texts));
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while calling the embedding service.", e);
        }
        if (response.statusCode() != 200) {
            String snippet = response.body().length() > 200 ? response.body().substring(0, 200) : response.body();
            throw new IOException("Embedding service returned HTTP " + response.statusCode() + ": " + snippet);
        }
        JsonNode rows = mapper.readTree(response.body()).get("embeddings");
        if (rows == null || !rows.isArray()) {
            throw new IOException("Embedding service response has no embeddings array.");
        }
        List<float[]> out = new ArrayList<>(rows.size());
        for (JsonNode row : rows) {
            float[] vector = new float[row.size()];
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) row.get(i).asDouble();
            }
            out.add(vector);
        }
        return out;
    }
}
