package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.entity.History;
import com.secondhand.note.repository.HistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Records note browsing history for the current user.
 */
@Service
public class HistoryAddService {

    @Resource
    private HistoryRepository historyRepository;

    /**
     * Creates or reactivates the history entry for a viewed note.
     */
    public ApiResponse<Void> addHistory(Long currentUserId, Long noteId) {

        History h = historyRepository.findByUserIdAndNoteId(currentUserId, noteId).orElse(null);
        long now = System.currentTimeMillis() / 1000;

        if (h == null) {
            h = History.builder()
                    .userId(currentUserId)
                    .noteId(noteId)
                    .build();
            h.setUpdateAt(now);
            historyRepository.save(h);
            return ApiResponse.success();
        }

        h.setStatus(Status.ACTIVE);
        h.setUpdateAt(now);
        historyRepository.save(h);

        return ApiResponse.success();
    }
}
