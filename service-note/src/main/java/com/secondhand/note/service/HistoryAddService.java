package com.secondhand.note.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.History;
import com.secondhand.note.repository.HistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Document(desc = "增加历史", module = "note", name = "增加历史", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-01-09",
        request = {
                @ApiParam(name = "noteId", value = "帖子id", dataType = "long")
        })
@Component("historyAddService1.0")
public class HistoryAddService extends ReflectApiService {

    @Resource
    private HistoryRepository historyRepository;

    public Map<String, Object> service(Long currentUserId, Long noteId) {

        History h = historyRepository.findByUserIdAndNoteId(currentUserId, noteId).orElse(null);
        long now = System.currentTimeMillis() / 1000;

        if (h == null) {
            h = History.builder()
                    .userId(currentUserId)
                    .noteId(noteId)
                    .build();
            h.setUpdateAt(now);
            historyRepository.save(h);
            return success();
        }

        h.setStatus(Status.ACTIVE);
        h.setUpdateAt(now);
        historyRepository.save(h);

        return success();
    }
}
