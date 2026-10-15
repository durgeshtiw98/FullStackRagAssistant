package com.example.rag.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** In-memory index with exact cosine search. Vectors are normalized on insert, so search is a dot product. */
public final class VectorIndex {

    private final List<Chunk> chunks = new ArrayList<>();
    private final List<float[]> vectors = new ArrayList<>();
    private int dimension = 0;

    public synchronized void add(Chunk chunk, float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector must not be empty.");
        }
        if (dimension == 0) {
            dimension = vector.length;
        } else if (vector.length != dimension) {
            throw new IllegalArgumentException("Expected dimension " + dimension + " but got " + vector.length);
        }
        chunks.add(chunk);
        vectors.add(normalized(vector));
    }

    public synchronized List<ScoredChunk> search(float[] query, int k) {
        if (chunks.isEmpty() || k <= 0) {
            return List.of();
        }
        if (query.length != dimension) {
            throw new IllegalArgumentException("Expected dimension " + dimension + " but got " + query.length);
        }
        float[] q = normalized(query);
        PriorityQueue<ScoredChunk> heap = new PriorityQueue<>(Comparator.comparingDouble(ScoredChunk::score));
        for (int i = 0; i < chunks.size(); i++) {
            float score = dot(q, vectors.get(i));
            if (heap.size() < k) {
                heap.add(new ScoredChunk(chunks.get(i), score));
            } else if (score > heap.peek().score()) {
                heap.poll();
                heap.add(new ScoredChunk(chunks.get(i), score));
            }
        }
        List<ScoredChunk> result = new ArrayList<>(heap);
        result.sort(Comparator.comparingDouble(ScoredChunk::score).reversed()
                .thenComparing(sc -> sc.chunk().id()));
        return result;
    }

    public synchronized int size() {
        return chunks.size();
    }

    public synchronized int dimension() {
        return dimension;
    }

    public synchronized List<Chunk> chunks() {
        return new ArrayList<>(chunks);
    }

    public synchronized List<float[]> vectors() {
        return new ArrayList<>(vectors);
    }

    private static float dot(float[] a, float[] b) {
        float sum = 0f;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static float[] normalized(float[] v) {
        double sumSquares = 0;
        for (float x : v) {
            sumSquares += (double) x * x;
        }
        if (sumSquares == 0) {
            throw new IllegalArgumentException("Cannot normalize a zero vector.");
        }
        float norm = (float) Math.sqrt(sumSquares);
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            out[i] = v[i] / norm;
        }
        return out;
    }
}
