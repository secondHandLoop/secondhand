package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.entity.Collection;
import com.secondhand.note.repository.CollectionRepository;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Creates or reactivates a user's collection record for a note.
 */
@Service
public class CollectionAddService {

    @Resource
    private CollectionRepository collectionRepository;

    @Resource
    private NoteRepository noteRepository;

    /**
     * Adds the target note to the current user's collection list.
     */
    public ApiResponse<Void> addCollection(Long currentUserId, Long noteId) {

        AssertUtil.isTrue(noteRepository.existsById(noteId), "帖子不存在");

        Collection c = collectionRepository.findByUserIdAndNoteId(currentUserId, noteId).orElse(null);

        long now = System.currentTimeMillis() / 1000;

        if (c == null) {
            c = Collection.builder()
                    .userId(currentUserId)
                    .noteId(noteId)
                    .build();
            collectionRepository.save(c);
            return ApiResponse.success();
        }

        if (c.getStatus() == Status.ACTIVE) {
            AssertUtil.isTrue(false, "已收藏过该帖子");
        }

        c.setStatus(Status.ACTIVE);
        c.setUpdateAt(now);
        collectionRepository.save(c);

        return ApiResponse.success();
    }
}
