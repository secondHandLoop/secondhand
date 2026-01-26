package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.History;
import com.secondhand.note.repository.HistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Document(desc = "删除历史", module = "note", name = "删除历史", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-01-09",
        request = {
                @ApiParam(name = "noteIds", value = "帖子ID", dataType = "List<Long>")
        })

@Component("historyDeleteService1.0")
public class HistoryDeleteService extends ReflectApiService {

    @Resource
    private HistoryRepository historyRepository;

    public Map<String, Object> service(Long uid, List<Long> noteIds) {

        AssertUtil.isTrue(noteIds != null && !noteIds.isEmpty(), "参数错误");

        List<History> histories =
                historyRepository.findByUserIdAndNoteIdIn(uid, noteIds);

        AssertUtil.isTrue(!histories.isEmpty(), "浏览记录不存在");

        long now = System.currentTimeMillis() / 1000;

        for (History h : histories) {
            if (h.getStatus() == Status.ACTIVE) {
                h.setStatus(Status.DEL);
                h.setUpdateAt(now);
            }
        }

        historyRepository.saveAll(histories);

        return success();
    }
}
