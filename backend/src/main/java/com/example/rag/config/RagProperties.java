package com.example.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Settings under the {@code rag} prefix in application.yml. */
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private String embeddingUrl = "http://localhost:8000";
    private String corpusDir = "../data/corpus";
    private String indexPath = "../data/index.bin";
    private int chunkWords = 80;
    private int overlapWords = 15;
    private int topK = 5;
    private double minScore = 0.05;
    private int embedBatchSize = 32;
    private final Groq groq = new Groq();

    public String getEmbeddingUrl() {
        return embeddingUrl;
    }

    public void setEmbeddingUrl(String embeddingUrl) {
        this.embeddingUrl = embeddingUrl;
    }

    public String getCorpusDir() {
        return corpusDir;
    }

    public void setCorpusDir(String corpusDir) {
        this.corpusDir = corpusDir;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    public int getChunkWords() {
        return chunkWords;
    }

    public void setChunkWords(int chunkWords) {
        this.chunkWords = chunkWords;
    }

    public int getOverlapWords() {
        return overlapWords;
    }

    public void setOverlapWords(int overlapWords) {
        this.overlapWords = overlapWords;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public int getEmbedBatchSize() {
        return embedBatchSize;
    }

    public void setEmbedBatchSize(int embedBatchSize) {
        this.embedBatchSize = embedBatchSize;
    }

    public Groq getGroq() {
        return groq;
    }

    public static class Groq {
        private String apiKey = "";
        private String model = "llama-3.3-70b-versatile";
        private String baseUrl = "https://api.groq.com/openai/v1";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
