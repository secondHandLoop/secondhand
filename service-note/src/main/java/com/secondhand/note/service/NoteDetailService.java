package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.dto.NoteDetailResponseBody;
import com.secondhand.note.dto.NoteResponseMapper;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.repository.CollectionRepository;
import com.secondhand.note.repository.NoteRepository;
import org.springframework.stereotype.Service;

/**
 * Loads note details for app clients and computes owner/collection flags.
 */
@Service
public class NoteDetailService {

    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;

    public NoteDetailService(NoteRepository noteRepository,
                             CollectionRepository collectionRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
    }

    /**
     * Returns the note detail view after applying ownership and publication checks.
     */
    public ApiResponse<NoteDetailResponseBody> getNoteDetail(Long currentUserId, Long noteId) {
        AssertUtil.notNull(noteId, "帖子ID不能为空");

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        AssertUtil.isTrue(note.getStatus() == Status.ACTIVE, "帖子已删除");

        boolean owner = currentUserId != null && currentUserId.equals(note.getUserId());
        boolean visible = note.getNoteStatus() == NoteStatus.PUBLISHED || owner;
        AssertUtil.isTrue(visible, "无权限查看该帖子");

        boolean collected = false;
        if (currentUserId != null) {
            collected = collectionRepository.findByUserIdAndNoteId(currentUserId, noteId)
                    .map(collection -> collection.getStatus() == Status.ACTIVE)
                    .orElse(false);
        }

        return ApiResponse.success(NoteResponseMapper.toNoteDetail(note, owner, collected));
    }
}
