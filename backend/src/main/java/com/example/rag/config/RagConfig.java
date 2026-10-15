package com.example.rag.config;

import com.example.rag.client.GroqAnswerGenerator;
import com.example.rag.client.HttpEmbeddingClient;
import com.example.rag.core.AnswerGenerator;
import com.example.rag.core.Chunker;
import com.example.rag.core.CorpusLoader;
import com.example.rag.core.DisabledAnswerGenerator;
import com.example.rag.core.EmbeddingClient;
import com.example.rag.core.IndexStore;
import com.example.rag.core.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RagConfig.class);

    @Bean
    public EmbeddingClient embeddingClient(RagProperties props) {
        return new HttpEmbeddingClient(props.getEmbeddingUrl());
    }

    @Bean
    public AnswerGenerator answerGenerator(RagProperties props) {
        RagProperties.Groq groq = props.getGroq();
        if (groq.getApiKey() == null || groq.getApiKey().isBlank()) {
            LOG.info("No GROQ_API_KEY set: answers will be extracted from the best matching passage.");
            return new DisabledAnswerGenerator();
        }
        LOG.info("Answer model: {} via {}", groq.getModel(), groq.getBaseUrl());
        return new GroqAnswerGenerator(groq.getBaseUrl(), groq.getApiKey(), groq.getModel());
    }

    @Bean
    public CorpusLoader corpusLoader() {
        return new CorpusLoader();
    }

    @Bean
    public RagService ragService(EmbeddingClient embeddings, AnswerGenerator generator, RagProperties props) {
        RagService service = new RagService(
                embeddings,
                generator,
                new Chunker(props.getChunkWords(), props.getOverlapWords()),
                new IndexStore(),
                Path.of(props.getIndexPath()),
                new RagService.Settings(props.getTopK(), props.getEmbedBatchSize(), props.getMinScore()));
        try {
            if (service.loadPersisted()) {
                LOG.info("Loaded saved index with {} chunks.", service.indexedChunks());
            } else {
                LOG.info("No saved index found. Call POST /api/ingest after starting the embedding service.");
            }
        } catch (IOException | RuntimeException e) {
            LOG.warn("Could not load the saved index: {}", e.getMessage());
        }
        return service;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                        .allowedMethods("GET", "POST");
            }
        };
    }
}
