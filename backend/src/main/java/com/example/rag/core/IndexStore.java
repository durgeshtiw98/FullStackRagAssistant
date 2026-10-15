package com.example.rag.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Saves and loads a {@link VectorIndex} in a compact binary file. */
public final class IndexStore {

    private static final int MAGIC = 0x52414731; // "RAG1"

    public void save(Path path, VectorIndex index) throws IOException {
        List<Chunk> chunks = index.chunks();
        List<float[]> vectors = index.vectors();
        Path absolute = path.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path tmp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeInt(MAGIC);
            out.writeInt(index.dimension());
            out.writeInt(chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                Chunk c = chunks.get(i);
                out.writeUTF(c.id());
                out.writeUTF(c.docTitle());
                out.writeUTF(c.url() == null ? "" : c.url());
                out.writeInt(c.index());
                out.writeUTF(c.text());
                for (float f : vectors.get(i)) {
                    out.writeFloat(f);
                }
            }
        }
        try {
            Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public VectorIndex load(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            if (in.readInt() != MAGIC) {
                throw new IOException("Not an index file: " + path);
            }
            int dimension = in.readInt();
            int count = in.readInt();
            VectorIndex index = new VectorIndex();
            for (int i = 0; i < count; i++) {
                String id = in.readUTF();
                String title = in.readUTF();
                String url = in.readUTF();
                int chunkIndex = in.readInt();
                String text = in.readUTF();
                float[] vector = new float[dimension];
                for (int d = 0; d < dimension; d++) {
                    vector[d] = in.readFloat();
                }
                index.add(new Chunk(id, title, url.isEmpty() ? null : url, chunkIndex, text), vector);
            }
            return index;
        }
    }
}
