package com.example.rag.web;

import com.example.rag.core.CorpusDocument;
import com.example.rag.core.CorpusLoader;
import com.example.rag.core.RagService;
import com.example.rag.config.RagProperties;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RagController {

    private final RagService service;
    private final CorpusLoader loader;
    private final RagProperties props;

    public RagController(RagService service, CorpusLoader loader, RagProperties props) {
        this.service = service;
        this.loader = loader;
        this.props = props;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", "ok");
        out.put("indexedChunks", service.indexedChunks());
        out.put("answerMode", service.answerModelEnabled() ? "llm" : "extractive");
        return out;
    }

    @GetMapping("/sources")
    public Map<String, Integer> sources() {
        return service.chunksPerDocument();
    }

    @PostMapping("/ask")
    public RagService.Answer ask(@Valid @RequestBody AskRequest request) throws IOException {
        return service.ask(request.question(), request.topK());
    }

    @PostMapping("/ingest")
    public RagService.IngestResult ingest() throws IOException {
        List<CorpusDocument> docs = loader.load(Path.of(props.getCorpusDir()));
        return service.ingest(docs);
    }
}
