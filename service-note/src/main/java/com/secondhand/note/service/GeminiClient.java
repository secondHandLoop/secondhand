package com.secondhand.note.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.secondhand.common.exception.ApiException;
import com.secondhand.note.config.GeminiProperties;
import com.secondhand.note.dto.ImageAttributesResponse;
import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.entity.TopCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Thin REST client for Google Gemini: identifies an item in a photo (vision)
 * and produces text embeddings for semantic search. Uses Spring's RestTemplate
 * and fastjson; no extra dependency required.
 */
@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    /** Gemini embedding task types (query vs stored document produce aligned spaces). */
    public static final String TASK_QUERY = "RETRIEVAL_QUERY";
    public static final String TASK_DOCUMENT = "RETRIEVAL_DOCUMENT";

    private static final String IDENTIFY_PROMPT =
            "You are tagging a photo for a secondhand marketplace listing. Look at the main item for sale and "
                    + "return a JSON object with these fields:\n"
                    + "- suggestedTitle: a concise 3-6 word marketplace title including the main color and item "
                    + "(e.g. \"Black Wooden Office Desk\").\n"
                    + "- item: the item's short name (e.g. \"desk\").\n"
                    + "- category: the single best match from this exact list, or null if unsure: "
                    + "ELECTRONICS, FURNITURE, HOME_APPLIANCES, CLOTHING, BOOKS, BEAUTY, SPORTS, TOYS, AUTOMOTIVE, FOOD, PET_SUPPLIES, OTHERS.\n"
                    + "- condition: the single best guess from this exact list, or null: NEW, LIKE_NEW, GOOD, FAIR, POOR.\n"
                    + "- colors: array of the item's dominant colors, lowercase.\n"
                    + "- shape: the item's overall shape, or null.\n"
                    + "- material: the item's main material, or null.\n"
                    + "- keywords: array of 3-8 extra descriptive words a buyer might search "
                    + "(brand if visible, style, features).\n"
                    + "Return ONLY the JSON object.";

    private final GeminiProperties properties;
    private final RestTemplate rest;

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        this.rest = new RestTemplate(factory);
    }

    /**
     * Identifies the item in a photo and returns marketplace attributes.
     *
     * @param imageBytes raw image bytes
     * @param mimeType   image content type, e.g. "image/jpeg" (defaults if null)
     * @return the identified attributes
     */
    public ImageAttributesResponse identifyImage(byte[] imageBytes, String mimeType) {
        JSONObject inlineData = new JSONObject();
        inlineData.put("mimeType", mimeType == null ? "image/jpeg" : mimeType);
        inlineData.put("data", Base64.getEncoder().encodeToString(imageBytes));
        JSONObject imagePart = new JSONObject();
        imagePart.put("inlineData", inlineData);
        JSONObject textPart = new JSONObject();
        textPart.put("text", IDENTIFY_PROMPT);

        JSONArray parts = new JSONArray();
        parts.add(imagePart);
        parts.add(textPart);
        JSONObject content = new JSONObject();
        content.put("parts", parts);
        JSONArray contents = new JSONArray();
        contents.add(content);

        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0.2);
        genConfig.put("responseMimeType", "application/json");

        JSONObject body = new JSONObject();
        body.put("contents", contents);
        body.put("generationConfig", genConfig);

        String url = properties.getBaseUrl() + "/models/" + properties.getVisionModel()
                + ":generateContent?key=" + properties.getApiKey();
        String response = post(url, body.toJSONString());

        // candidates[0].content.parts[0].text holds the JSON produced by the model.
        JSONObject root = JSON.parseObject(response);
        JSONArray candidates = root.getJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new ApiException("图片识别失败: 模型未返回结果", 500);
        }
        String jsonText = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        JSONObject attrs = JSON.parseObject(jsonText);
        return toAttributes(attrs);
    }

    /**
     * Produces an embedding vector for the given text.
     *
     * @param text     the text to embed
     * @param taskType {@link #TASK_QUERY} or {@link #TASK_DOCUMENT}
     * @return the embedding vector (length = configured embeddingDimensions)
     */
    public float[] embed(String text, String taskType) {
        JSONObject textPart = new JSONObject();
        textPart.put("text", text);
        JSONArray parts = new JSONArray();
        parts.add(textPart);
        JSONObject content = new JSONObject();
        content.put("parts", parts);

        JSONObject body = new JSONObject();
        body.put("model", "models/" + properties.getEmbeddingModel());
        body.put("content", content);
        body.put("taskType", taskType);
        body.put("outputDimensionality", properties.getEmbeddingDimensions());

        String url = properties.getBaseUrl() + "/models/" + properties.getEmbeddingModel()
                + ":embedContent?key=" + properties.getApiKey();
        String response = post(url, body.toJSONString());

        JSONArray values = JSON.parseObject(response)
                .getJSONObject("embedding")
                .getJSONArray("values");
        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.getFloatValue(i);
        }
        return vector;
    }

    private ImageAttributesResponse toAttributes(JSONObject attrs) {
        ImageAttributesResponse r = new ImageAttributesResponse();
        r.setSuggestedTitle(attrs.getString("suggestedTitle"));
        r.setItem(attrs.getString("item"));
        r.setCategory(normalizeCategory(attrs.getString("category")));
        r.setCondition(normalizeCondition(attrs.getString("condition")));
        r.setShape(attrs.getString("shape"));
        r.setMaterial(attrs.getString("material"));
        r.setColors(toStringList(attrs.getJSONArray("colors")));
        r.setKeywords(toStringList(attrs.getJSONArray("keywords")));
        return r;
    }

    private List<String> toStringList(JSONArray array) {
        if (array == null) {
            return null;
        }
        List<String> list = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            String v = array.getString(i);
            if (v != null && !v.trim().isEmpty()) {
                list.add(v.trim());
            }
        }
        return list;
    }

    /** Returns the enum name only if it is a valid TopCategory, else null. */
    private String normalizeCategory(String raw) {
        if (raw == null) {
            return null;
        }
        String candidate = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        try {
            return TopCategory.valueOf(candidate).name();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Returns the enum name only if it is a valid NoteCondition, else null. */
    private String normalizeCondition(String raw) {
        if (raw == null) {
            return null;
        }
        String candidate = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        try {
            return NoteCondition.valueOf(candidate).name();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String post(String url, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        try {
            ResponseEntity<String> response = rest.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (RestClientResponseException ex) {
            log.warn("Gemini request failed ({}): {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new ApiException("AI 服务请求失败 (" + ex.getStatusCode().value() + ")", 502);
        } catch (Exception ex) {
            log.warn("Gemini request error", ex);
            throw new ApiException("AI 服务连接失败: " + ex.getMessage(), 502);
        }
    }
}
