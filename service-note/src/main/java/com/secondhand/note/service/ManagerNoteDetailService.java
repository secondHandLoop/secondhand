package com.secondhand.note.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.dto.ManagerNoteDetailResponseBody;
import com.secondhand.note.dto.NoteResponseMapper;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.NoteRepository;
import org.springframework.stereotype.Service;

/**
 * Provides full note details for manager-facing moderation screens.
 */
@Service
public class ManagerNoteDetailService {

    private final NoteRepository noteRepository;

    public ManagerNoteDetailService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Loads a note by id without the public visibility restrictions used in the app feed.
     */
    public ApiResponse<ManagerNoteDetailResponseBody> getNoteDetailForManager(Long noteId) {
        AssertUtil.notNull(noteId, "帖子ID不能为空");

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        return ApiResponse.success(NoteResponseMapper.toManagerNoteDetail(note));
    }
}
