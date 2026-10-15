package com.example.rag.client;

import com.example.rag.core.AnswerGenerator;
import com.example.rag.core.Prompt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Calls an OpenAI compatible chat completions endpoint (Groq by default). */
public final class GroqAnswerGenerator implements AnswerGenerator {

    private static final int MAX_ATTEMPTS = 4;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI endpoint;
    private final String apiKey;
    private final String model;

    public GroqAnswerGenerator(String baseUrl, String apiKey, String model) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.endpoint = URI.create(trimmed + "/chat/completions");
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public String generate(Prompt prompt) throws IOException {
        String body = mapper.writeValueAsString(Map.of(
                "model", model,
                "temperature", 0,
                "max_tokens", 300,
                "messages", List.of(
                        Map.of("role", "system", "content", prompt.system()),
                        Map.of("role", "user", "content", prompt.user()))));
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        for (int attempt = 1; ; attempt++) {
            HttpResponse<String> response;
            try {
                response = http.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while calling the answer model.", e);
            }
            if (response.statusCode() == 429 && attempt < MAX_ATTEMPTS) {
                sleepSeconds(retryAfterSeconds(response));
                continue;
            }
            if (response.statusCode() != 200) {
                String text = response.body();
                throw new IOException("Answer model returned HTTP " + response.statusCode() + ": "
                        + (text.length() > 200 ? text.substring(0, 200) : text));
            }
            JsonNode content = mapper.readTree(response.body()).path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new IOException("Answer model response has no message content.");
            }
            return content.asText();
        }
    }

    private static long retryAfterSeconds(HttpResponse<String> response) {
        String header = response.headers().firstValue("retry-after").orElse("5");
        try {
            return Math.max(1, Math.min(30, (long) Math.ceil(Double.parseDouble(header.trim()))));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private static void sleepSeconds(long seconds) throws IOException {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting to retry.", e);
        }
    }
}
