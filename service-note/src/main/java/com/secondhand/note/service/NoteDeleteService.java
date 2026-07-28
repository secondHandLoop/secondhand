package com.secondhand.note.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.CollectionRepository;
import com.secondhand.note.repository.HistoryRepository;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Hard-deletes notes: removes the note row and every reference to it, and deletes
 * its stored photos, so nothing about the listing is left behind.
 */
@Service
public class NoteDeleteService {

    private static final Logger log = LoggerFactory.getLogger(NoteDeleteService.class);

    @Resource
    NoteRepository noteRepository;

    @Resource
    CollectionRepository collectionRepository;

    @Resource
    HistoryRepository historyRepository;

    @Resource
    NotePhotoStorageService notePhotoStorageService;

    /**
     * Permanently deletes a note owned by the current user, along with any collections
     * and browsing history that reference it and its stored photos.
     *
     * @param currentUserId the authenticated user's id
     * @param noteId        the note to delete
     * @return an empty success response
     */
    @Transactional
    public ApiResponse<Void> deleteNote(Long currentUserId,
                                        Long noteId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        AssertUtil.isTrue(currentUserId != null && currentUserId.equals(note.getUserId()), "无权限删除该帖子");

        List<String> photoUrls = note.getPhotoList();

        // Remove every reference to the note (any user's favorites/history) before the
        // note itself, so no rows are left pointing at a note that no longer exists.
        collectionRepository.deleteByNoteId(noteId);
        historyRepository.deleteByNoteId(noteId);
        noteRepository.delete(note);

        // Best-effort media cleanup: a storage hiccup must not fail (or roll back) the
        // delete — the DB removal is the source of truth.
        try {
            notePhotoStorageService.deleteByUrls(photoUrls);
        } catch (Exception ex) {
            log.warn("Failed to delete R2 photos for note {}: {}", noteId, ex.getMessage());
        }

        return ApiResponse.success();
    }
}
