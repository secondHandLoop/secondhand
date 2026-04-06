package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.dto.ApiResponse;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Soft-deletes notes and removes their stored photos.
 */
@Service
public class NoteDeleteService {

    @Resource
    NoteRepository noteRepository;

    @Resource
    NotePhotoStorageService notePhotoStorageService;

    /**
     * Deletes a note only when it belongs to the current user and is still active.
     */
    public ApiResponse<Void> deleteNote(Long currentUserId,
                                        Long noteId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        AssertUtil.isTrue(note.getUserId() == currentUserId, "无权限删除该帖子");

        AssertUtil.isTrue(note.getStatus() != Status.DEL, "帖子已删除");

        note.setStatus(Status.DEL);
        noteRepository.save(note);
        notePhotoStorageService.deleteByUrls(note.getPhotoList());

        return ApiResponse.success();
    }
}
