package com.secondhand.note.service;

import com.secondhand.common.exception.ApiException;
import com.secondhand.note.config.GeminiProperties;
import com.secondhand.note.dto.ApiResponse;
import com.secondhand.note.dto.ImageAttributesResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Identifies the item in an uploaded photo so the create-listing form can be
 * auto-filled (title, category, condition, etc.).
 */
@Service
public class PhotoAnalyzeService {

    private final GeminiProperties properties;
    private final GeminiClient geminiClient;

    public PhotoAnalyzeService(GeminiProperties properties, GeminiClient geminiClient) {
        this.properties = properties;
        this.geminiClient = geminiClient;
    }

    /**
     * Analyzes a listing photo and returns the identified attributes.
     *
     * @param file the uploaded image
     * @return the identified attributes
     */
    public ApiResponse<ImageAttributesResponse> analyze(MultipartFile file) {
        if (!properties.isEnabled()) {
            throw new ApiException("图片识别未启用", 503);
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException("请上传图片", 400);
        }
        try {
            ImageAttributesResponse attributes = geminiClient.identifyImage(file.getBytes(), file.getContentType());
            return ApiResponse.success(attributes);
        } catch (IOException ex) {
            throw new ApiException("读取图片失败: " + ex.getMessage(), 400);
        }
    }
}
