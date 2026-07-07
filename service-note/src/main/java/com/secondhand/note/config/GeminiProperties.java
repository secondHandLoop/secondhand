package com.secondhand.note.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for Google Gemini, used both to identify listing photos (vision)
 * and to produce text embeddings for semantic search. Bound from {@code ai.gemini.*}.
 */
@Component
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiProperties {

    /** Master switch. When false, photo analysis and embeddings are skipped. */
    private boolean enabled = false;

    /** Google AI Studio API key. */
    private String apiKey = "";

    /** Base URL of the Generative Language API. */
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

    /** Vision model used to identify an item in a photo. */
    private String visionModel = "gemini-2.5-flash-lite";

    /** Text-embedding model. */
    private String embeddingModel = "gemini-embedding-001";

    /** Embedding vector size (must match the Postgres vector(N) column). */
    private int embeddingDimensions = 768;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getVisionModel() { return visionModel; }
    public void setVisionModel(String visionModel) { this.visionModel = visionModel; }

    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

    public int getEmbeddingDimensions() { return embeddingDimensions; }
    public void setEmbeddingDimensions(int embeddingDimensions) { this.embeddingDimensions = embeddingDimensions; }
}
