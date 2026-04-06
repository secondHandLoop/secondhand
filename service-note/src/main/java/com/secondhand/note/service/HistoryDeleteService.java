package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.dto.ApiResponse;
import com.secondhand.note.entity.History;
import com.secondhand.note.repository.HistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Soft-deletes history entries in batches for the current user.
 */
@Service
public class HistoryDeleteService {

    @Resource
    private HistoryRepository historyRepository;

    /**
     * Marks the selected history records as deleted.
     */
    public ApiResponse<Void> deleteHistories(Long currentUserId, List<Long> noteIds) {

        AssertUtil.isTrue(noteIds != null && !noteIds.isEmpty(), "参数错误");

        List<History> histories =
                historyRepository.findByUserIdAndNoteIdIn(currentUserId, noteIds);

        AssertUtil.isTrue(!histories.isEmpty(), "浏览记录不存在");

        long now = System.currentTimeMillis() / 1000;

        for (History h : histories) {
            if (h.getStatus() == Status.ACTIVE) {
                h.setStatus(Status.DEL);
                h.setUpdateAt(now);
            }
        }

        historyRepository.saveAll(histories);

        return ApiResponse.success();
    }
}
