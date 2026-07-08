package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.entity.Collection;
import com.secondhand.note.repository.CollectionRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Soft-deletes collection records owned by the current user.
 */
@Service
public class CollectionDeleteService {

    @Resource
    private CollectionRepository collectionRepository;

    /**
     * Removes a note from the current user's collection list.
     */
    public ApiResponse<Void> deleteCollection(Long currentUserId, Long noteId) {

        Collection c = collectionRepository.findByUserIdAndNoteId(currentUserId, noteId).orElse(null);
        AssertUtil.isTrue(c != null, "收藏记录不存在");
        AssertUtil.isTrue(c.getStatus() == Status.ACTIVE, "收藏记录已删除");

        long now = System.currentTimeMillis() / 1000;
        c.setStatus(Status.DEL);
        c.setUpdateAt(now);
        collectionRepository.save(c);

        return ApiResponse.success();
    }
}
