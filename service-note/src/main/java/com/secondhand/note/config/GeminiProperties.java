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
    private String visionModel = "gemini-flash-lite-latest";

    /**
     * Reasoning level for Gemini 3.x vision models: minimal|low|medium|high. Photo
     * tagging needs no reasoning, so "minimal" is the default (the default model is a
     * 3.x flash-lite). When set, it takes priority over {@link #thinkingBudget}. Blank
     * it and set {@link #thinkingBudget} instead only for a Gemini 2.5 model.
     */
    private String thinkingLevel = "minimal";

    /**
     * Reasoning token budget for Gemini 2.5 vision models; 0 disables thinking entirely.
     * Applies only when {@link #thinkingLevel} is blank — Gemini rejects both at once.
     */
    private Integer thinkingBudget = 0;

    /** Text-embedding model. */
    private String embeddingModel = "gemini-embedding-001";

    /** Embedding vector size (must match the Postgres vector(N) column). */
    private int embeddingDimensions = 768;

    /**
     * Longest-side cap (px) for images sent to the vision model. 768 = one Gemini
     * tile ≈ 258 image tokens. Raise it (e.g. 4096) to measure the un-downscaled
     * token cost — the per-call `usageMetadata` is logged, so the two runs give the
     * exact savings.
     */
    private int aiImageMaxDim = 768;

    /** JPEG quality for images sent to the vision model. */
    private float aiImageQuality = 0.85f;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getVisionModel() { return visionModel; }
    public void setVisionModel(String visionModel) { this.visionModel = visionModel; }

    public String getThinkingLevel() { return thinkingLevel; }
    public void setThinkingLevel(String thinkingLevel) { this.thinkingLevel = thinkingLevel; }

    public Integer getThinkingBudget() { return thinkingBudget; }
    public void setThinkingBudget(Integer thinkingBudget) { this.thinkingBudget = thinkingBudget; }

    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

    public int getEmbeddingDimensions() { return embeddingDimensions; }
    public void setEmbeddingDimensions(int embeddingDimensions) { this.embeddingDimensions = embeddingDimensions; }

    public int getAiImageMaxDim() { return aiImageMaxDim; }
    public void setAiImageMaxDim(int aiImageMaxDim) { this.aiImageMaxDim = aiImageMaxDim; }

    public float getAiImageQuality() { return aiImageQuality; }
    public void setAiImageQuality(float aiImageQuality) { this.aiImageQuality = aiImageQuality; }
}
