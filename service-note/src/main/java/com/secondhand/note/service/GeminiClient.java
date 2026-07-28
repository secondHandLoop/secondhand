package com.secondhand.note.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.secondhand.common.exception.ApiException;
import com.secondhand.note.config.GeminiProperties;
import com.secondhand.note.dto.ImageAttributesResponse;
import com.secondhand.note.dto.PhotoGroupResponse;
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

    private static final String GROUP_PROMPT =
            "You are helping a user create secondhand marketplace listings from a batch of photos. "
                    + "You will be shown photos labeled \"Image <index>:\" (0-indexed). Some photos show the SAME "
                    + "physical item (multiple angles or close-ups of one object); others show DIFFERENT items. "
                    + "Group the photos so that each group contains all and only the photos of ONE physical item. "
                    + "Every image index must appear in exactly one group. For each group, return listing info. "
                    + "Return ONLY a JSON object of this shape:\n"
                    + "{\"groups\":[{\"indices\":[0,2],\"suggestedTitle\":\"Bed\",\"category\":\"FURNITURE\","
                    + "\"condition\":\"GOOD\",\"attributes\":{\"colors\":[\"grey\",\"white\"],\"material\":\"wood\","
                    + "\"shape\":\"rectangular\",\"keywords\":[\"double\",\"frame\"]}}]}\n"
                    + "suggestedTitle must be the SHORT item name ONLY — 1-3 words, no colors or adjectives "
                    + "(e.g. \"Bed\", \"Steamer Pot\", \"Electric Kettle\"). Put all descriptive detail in "
                    + "attributes: colors (array of dominant colors, lowercase), material, shape, and keywords "
                    + "(3-8 extra words a buyer might search: brand if visible, style, features). Use null for any "
                    + "unknown attribute. category must be one of ELECTRONICS, FURNITURE, HOME_APPLIANCES, CLOTHING, "
                    + "BOOKS, BEAUTY, SPORTS, TOYS, AUTOMOTIVE, FOOD, PET_SUPPLIES, OTHERS, or null. condition must "
                    + "be one of NEW, LIKE_NEW, GOOD, FAIR, POOR, or null.";

    private final GeminiProperties properties;
    private final ImageCompressor imageCompressor;
    private final RestTemplate rest;

    public GeminiClient(GeminiProperties properties, ImageCompressor imageCompressor) {
        this.properties = properties;
        this.imageCompressor = imageCompressor;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        this.rest = new RestTemplate(factory);
    }

    /**
     * Adds a thinkingConfig so photo tagging doesn't pay for reasoning tokens. Sends
     * thinkingLevel (Gemini 3.x) when configured, else thinkingBudget (Gemini 2.5);
     * never both, which Gemini rejects. Omitted entirely when neither is set.
     *
     * @param genConfig the generationConfig object to augment
     */
    private void applyThinkingConfig(JSONObject genConfig) {
        String level = properties.getThinkingLevel();
        Integer budget = properties.getThinkingBudget();
        JSONObject thinking = new JSONObject();
        if (level != null && !level.trim().isEmpty()) {
            thinking.put("thinkingLevel", level.trim());
        } else if (budget != null) {
            thinking.put("thinkingBudget", budget);
        } else {
            return;
        }
        genConfig.put("thinkingConfig", thinking);
    }

    /**
     * Logs the token usage Gemini reports for a call, so vision cost is observable.
     * A high "thoughts" count means the model is still reasoning — check thinkingLevel.
     *
     * @param label short call name, e.g. "identify" or "group"
     * @param root  the parsed response body
     */
    private void logUsage(String label, int imageCount, JSONObject root) {
        if (root == null) {
            return;
        }
        JSONObject usage = root.getJSONObject("usageMetadata");
        if (usage == null) {
            return;
        }
        // images + cap make the line self-documenting: re-run with a larger
        // ai-image-max-dim to get the un-downscaled prompt cost and compute the savings.
        log.info("Gemini {} tokens (images={}, cap={}px): prompt={}, output={}, thoughts={}, total={}",
                label,
                imageCount,
                properties.getAiImageMaxDim(),
                usage.getInteger("promptTokenCount"),
                usage.getInteger("candidatesTokenCount"),
                usage.getInteger("thoughtsTokenCount"),
                usage.getInteger("totalTokenCount"));
    }

    /** Downscales an image to {@code ai.gemini.ai-image-max-dim} and JPEG-encodes it to minimize vision-call tokens. */
    private byte[] downscaleForAi(byte[] imageBytes) {
        // Always re-encode (skip budget 0): guarantees the JPEG the request advertises,
        // and the size cap must apply even to an already-small original.
        byte[] out = imageCompressor.compressToJpeg(
                imageBytes, properties.getAiImageMaxDim(), properties.getAiImageQuality(), 0).bytes();
        // Diagnostic: shows the exact pixels actually sent to Gemini, so a cap change
        // can be confirmed (different WxH) vs. Gemini normalizing (same tokens anyway).
        if (log.isInfoEnabled()) {
            try {
                java.awt.image.BufferedImage img =
                        javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(out));
                log.info("AI image (cap={}px): {} bytes in -> {} bytes out, {}x{} sent",
                        properties.getAiImageMaxDim(), imageBytes.length, out.length,
                        img == null ? -1 : img.getWidth(), img == null ? -1 : img.getHeight());
            } catch (Exception ignore) {
            }
        }
        return out;
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
        inlineData.put("mimeType", "image/jpeg");
        inlineData.put("data", Base64.getEncoder().encodeToString(downscaleForAi(imageBytes)));
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
        applyThinkingConfig(genConfig);

        JSONObject body = new JSONObject();
        body.put("contents", contents);
        body.put("generationConfig", genConfig);

        String url = properties.getBaseUrl() + "/models/" + properties.getVisionModel()
                + ":generateContent?key=" + properties.getApiKey();
        String response = post(url, body.toJSONString());

        // candidates[0].content.parts[0].text holds the JSON produced by the model.
        JSONObject root = JSON.parseObject(response);
        logUsage("identify", 1, root);
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
     * Groups a batch of photos by physical item in a single multimodal call, so
     * the bulk create-listing flow can turn each group into one listing draft.
     *
     * @param images    raw image bytes, in upload order
     * @param mimeTypes content type per image (same order; null defaults to image/jpeg)
     * @return groups of photo indices, each with suggested listing attributes
     */
    public PhotoGroupResponse groupImages(List<byte[]> images, List<String> mimeTypes) {
        JSONArray parts = new JSONArray();
        JSONObject header = new JSONObject();
        header.put("text", GROUP_PROMPT);
        parts.add(header);

        for (int i = 0; i < images.size(); i++) {
            JSONObject label = new JSONObject();
            label.put("text", "Image " + i + ":");
            parts.add(label);

            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/jpeg");
            inlineData.put("data", Base64.getEncoder().encodeToString(downscaleForAi(images.get(i))));
            JSONObject imagePart = new JSONObject();
            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);
        }

        JSONObject content = new JSONObject();
        content.put("parts", parts);
        JSONArray contents = new JSONArray();
        contents.add(content);

        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0.1);
        genConfig.put("responseMimeType", "application/json");
        applyThinkingConfig(genConfig);

        JSONObject body = new JSONObject();
        body.put("contents", contents);
        body.put("generationConfig", genConfig);

        String url = properties.getBaseUrl() + "/models/" + properties.getVisionModel()
                + ":generateContent?key=" + properties.getApiKey();
        String response = post(url, body.toJSONString());

        try {
            JSONObject root = JSON.parseObject(response);
            logUsage("group", images.size(), root);
            JSONArray candidates = root.getJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return perImageGroups(images.size());
            }
            String jsonText = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            return normalizeGroups(JSON.parseObject(jsonText), images.size());
        } catch (Exception ex) {
            // Never fail the upload over a bad grouping response — degrade to one draft per photo.
            log.warn("Photo grouping parse failed; falling back to one group per photo", ex);
            return perImageGroups(images.size());
        }
    }

    /**
     * Coerces the model's groups into a valid result: keeps only in-range,
     * non-duplicate indices, and adds any photo the model dropped as its own group.
     */
    private PhotoGroupResponse normalizeGroups(JSONObject parsed, int n) {
        List<PhotoGroupResponse.Group> out = new ArrayList<>();
        boolean[] covered = new boolean[n];

        JSONArray groupsArr = parsed == null ? null : parsed.getJSONArray("groups");
        if (groupsArr != null) {
            for (int g = 0; g < groupsArr.size(); g++) {
                JSONObject gj = groupsArr.getJSONObject(g);
                if (gj == null) {
                    continue;
                }
                List<Integer> idx = new ArrayList<>();
                JSONArray idxArr = gj.getJSONArray("indices");
                if (idxArr != null) {
                    for (int k = 0; k < idxArr.size(); k++) {
                        Integer v = idxArr.getInteger(k);
                        if (v != null && v >= 0 && v < n && !covered[v]) {
                            covered[v] = true;
                            idx.add(v);
                        }
                    }
                }
                if (idx.isEmpty()) {
                    continue;
                }
                PhotoGroupResponse.Group group = new PhotoGroupResponse.Group();
                group.setIndices(idx);
                group.setSuggestedTitle(trimToNull(gj.getString("suggestedTitle")));
                group.setCategory(normalizeCategory(gj.getString("category")));
                group.setCondition(normalizeCondition(gj.getString("condition")));
                JSONObject attrs = gj.getJSONObject("attributes");
                group.setAttributes(attrs != null ? attrs.toJSONString() : null);
                out.add(group);
            }
        }

        for (int i = 0; i < n; i++) {
            if (!covered[i]) {
                List<Integer> idx = new ArrayList<>();
                idx.add(i);
                PhotoGroupResponse.Group group = new PhotoGroupResponse.Group();
                group.setIndices(idx);
                out.add(group);
            }
        }

        PhotoGroupResponse resp = new PhotoGroupResponse();
        resp.setGroups(out);
        return resp;
    }

    /** Fallback: each photo becomes its own single-photo group. */
    private PhotoGroupResponse perImageGroups(int n) {
        List<PhotoGroupResponse.Group> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> idx = new ArrayList<>();
            idx.add(i);
            PhotoGroupResponse.Group group = new PhotoGroupResponse.Group();
            group.setIndices(idx);
            out.add(group);
        }
        PhotoGroupResponse resp = new PhotoGroupResponse();
        resp.setGroups(out);
        return resp;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
