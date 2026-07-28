package com.secondhand.note.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.secondhand.note.config.GeminiProperties;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Owns the pgvector "embedding" column on the note table: it bootstraps the
 * column/index on startup, writes an embedding whenever a note is saved, and
 * runs semantic (vector) search. All operations degrade gracefully — if pgvector
 * is unavailable or Gemini is disabled, embeddings are skipped and callers fall
 * back to keyword search.
 */
@Service
public class NoteEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(NoteEmbeddingService.class);

    private final GeminiProperties properties;
    private final GeminiClient geminiClient;
    private final JdbcTemplate jdbcTemplate;
    private final NoteRepository noteRepository;

    /** True once the pgvector column + index exist and Gemini is enabled. */
    private volatile boolean ready = false;

    public NoteEmbeddingService(GeminiProperties properties, GeminiClient geminiClient,
                                JdbcTemplate jdbcTemplate, NoteRepository noteRepository) {
        this.properties = properties;
        this.geminiClient = geminiClient;
        this.jdbcTemplate = jdbcTemplate;
        this.noteRepository = noteRepository;
    }

    /**
     * Ensures the pgvector extension, the embedding column, and its HNSW index
     * exist. Runs once at startup; a failure (e.g. the Postgres image lacks
     * pgvector) disables semantic features without stopping the app.
     */
    @PostConstruct
    public void ensureSchema() {
        if (!properties.isEnabled()) {
            log.info("Gemini disabled (ai.gemini.enabled=false) — semantic search off; keyword search still works.");
            return;
        }
        int dims = properties.getEmbeddingDimensions();
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jdbcTemplate.execute("ALTER TABLE note ADD COLUMN IF NOT EXISTS embedding vector(" + dims + ")");
            jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS note_embedding_idx ON note USING hnsw (embedding vector_cosine_ops)");
            ready = true;
            log.info("pgvector ready — semantic search enabled ({} dims).", dims);
        } catch (Exception ex) {
            ready = false;
            log.warn("pgvector unavailable — semantic search disabled, falling back to keyword search. "
                    + "Use the 'pgvector/pgvector:pg16' Postgres image to enable it. Cause: {}", ex.getMessage());
        }
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * Embeds a note and writes its vector in the background so note create/update
     * is not blocked by the embedding round-trip. No-op when semantic search is off.
     */
    public void embedNoteAsync(Note note) {
        if (!ready || note == null || note.getId() == null) {
            return;
        }
        final Long id = note.getId();
        final String text = buildEmbeddingText(note);
        if (!StringUtils.hasText(text)) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                float[] vector = geminiClient.embed(text, GeminiClient.TASK_DOCUMENT);
                jdbcTemplate.update("UPDATE note SET embedding = ?::vector WHERE id = ?", toVectorLiteral(vector), id);
            } catch (Exception ex) {
                log.warn("Failed to embed note {}: {}", id, ex.getMessage());
            }
        });
    }

    /**
     * Semantic search: returns note ids ordered by similarity to the query text,
     * honoring the same visibility + category/delivery filters as keyword search.
     *
     * @return ordered ids, or null when semantic search is unavailable (caller
     *         should fall back to keyword search)
     */
    public List<Long> semanticSearchIds(String queryText, Long currentUserId, TopCategory topCategory,
                                        Boolean delivery, int page, int limit) {
        if (!ready || !StringUtils.hasText(queryText)) {
            return null;
        }
        try {
            float[] queryVector = geminiClient.embed(queryText, GeminiClient.TASK_QUERY);
            String vectorLiteral = toVectorLiteral(queryVector);

            StringBuilder sql = new StringBuilder(
                    "SELECT id FROM note WHERE status = 'ACTIVE' AND note_status = 'PUBLISHED' AND embedding IS NOT NULL");
            List<Object> params = new ArrayList<>();
            if (currentUserId != null) {
                sql.append(" AND user_id <> ?");
                params.add(currentUserId);
            }
            if (topCategory != null) {
                sql.append(" AND top_category = ?");
                params.add(topCategory.name());
            }
            if (delivery != null) {
                sql.append(" AND delivery = ?");
                params.add(delivery);
            }
            sql.append(" ORDER BY embedding <=> ?::vector LIMIT ? OFFSET ?");
            params.add(vectorLiteral);
            params.add(limit);
            params.add((page - 1) * limit);

            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> rs.getLong("id"), params.toArray());
        } catch (Exception ex) {
            log.warn("Semantic search failed, falling back to keyword: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Backfills embeddings for every active note that has none yet. Intended to be
     * called once after enabling the feature. Returns the number embedded.
     */
    public int reembedMissing() {
        if (!ready) {
            return 0;
        }
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM note WHERE status = 'ACTIVE' AND embedding IS NULL", Long.class);
        int done = 0;
        for (Long id : ids) {
            Note note = noteRepository.findById(id).orElse(null);
            if (note == null) {
                continue;
            }
            String text = buildEmbeddingText(note);
            if (!StringUtils.hasText(text)) {
                continue;
            }
            try {
                float[] vector = geminiClient.embed(text, GeminiClient.TASK_DOCUMENT);
                jdbcTemplate.update("UPDATE note SET embedding = ?::vector WHERE id = ?", toVectorLiteral(vector), id);
                done++;
            } catch (Exception ex) {
                log.warn("Backfill embed failed for note {}: {}", id, ex.getMessage());
            }
        }
        log.info("Re-embedded {} notes.", done);
        return done;
    }

    /** Builds the natural-language text that represents a note for embedding. */
    private String buildEmbeddingText(Note note) {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, note.getTitle());
        appendPart(sb, note.getDescription());
        appendPart(sb, attributesToText(note.getAttributes()));
        if (note.getTopCategory() != null) {
            appendPart(sb, note.getTopCategory().name().toLowerCase().replace('_', ' '));
        }
        if (note.getNoteCondition() != null) {
            appendPart(sb, note.getNoteCondition().name().toLowerCase().replace('_', ' ') + " condition");
        }
        if (StringUtils.hasText(note.getCity()) || StringUtils.hasText(note.getState())) {
            appendPart(sb, join(note.getCity(), note.getState()));
        }
        return sb.toString().trim();
    }

    /** Flattens the AI attributes JSON (colors, material, shape, keywords) into search text. */
    private String attributesToText(String attributesJson) {
        if (!StringUtils.hasText(attributesJson)) {
            return null;
        }
        try {
            JSONObject o = JSON.parseObject(attributesJson);
            List<String> parts = new ArrayList<>();
            collect(parts, o.get("colors"));
            collect(parts, o.get("material"));
            collect(parts, o.get("shape"));
            collect(parts, o.get("keywords"));
            return parts.isEmpty() ? null : String.join(" ", parts);
        } catch (Exception ex) {
            return null;
        }
    }

    private void collect(List<String> parts, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            for (int i = 0; i < arr.size(); i++) {
                String s = arr.getString(i);
                if (StringUtils.hasText(s)) {
                    parts.add(s.trim());
                }
            }
        } else {
            String s = String.valueOf(value).trim();
            if (StringUtils.hasText(s) && !"null".equals(s)) {
                parts.add(s);
            }
        }
    }

    private void appendPart(StringBuilder sb, String part) {
        if (StringUtils.hasText(part)) {
            if (sb.length() > 0) {
                sb.append(". ");
            }
            sb.append(part.trim());
        }
    }

    private String join(String a, String b) {
        if (StringUtils.hasText(a) && StringUtils.hasText(b)) {
            return a + ", " + b;
        }
        return StringUtils.hasText(a) ? a : b;
    }

    /** Formats a vector as the pgvector text literal "[v1,v2,...]". */
    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8);
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
