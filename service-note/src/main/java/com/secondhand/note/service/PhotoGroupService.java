package com.secondhand.note.service;

import com.secondhand.common.exception.ApiException;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.config.GeminiProperties;
import com.secondhand.note.dto.PhotoGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Groups a batch of uploaded photos by physical item so the bulk create-listing
 * flow can turn each group into one listing draft.
 */
@Service
public class PhotoGroupService {

    /** Cap the images per call to keep the multimodal request bounded. */
    private static final int MAX_PHOTOS = 30;

    private final GeminiProperties properties;
    private final GeminiClient geminiClient;

    public PhotoGroupService(GeminiProperties properties, GeminiClient geminiClient) {
        this.properties = properties;
        this.geminiClient = geminiClient;
    }

    /**
     * Groups the uploaded photos by item.
     *
     * @param files the uploaded images, in order
     * @return one group per detected item, referencing 0-based photo indices
     */
    public ApiResponse<PhotoGroupResponse> group(List<MultipartFile> files) {
        if (!properties.isEnabled()) {
            throw new ApiException("图片识别未启用", 503);
        }
        if (files == null || files.isEmpty()) {
            throw new ApiException("请上传图片", 400);
        }
        if (files.size() > MAX_PHOTOS) {
            throw new ApiException("单次最多分组 " + MAX_PHOTOS + " 张图片", 400);
        }

        List<byte[]> images = new ArrayList<>(files.size());
        List<String> mimeTypes = new ArrayList<>(files.size());
        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    throw new ApiException("请上传有效的图片", 400);
                }
                images.add(file.getBytes());
                mimeTypes.add(file.getContentType());
            }
        } catch (IOException ex) {
            throw new ApiException("读取图片失败: " + ex.getMessage(), 400);
        }

        return ApiResponse.success(geminiClient.groupImages(images, mimeTypes));
    }
}
