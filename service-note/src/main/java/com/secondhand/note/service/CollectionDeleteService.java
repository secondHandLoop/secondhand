package com.secondhand.note.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.Collection;
import com.secondhand.note.repository.CollectionRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Document(desc = "删除收藏", module = "note", name = "删除收藏", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-01-09",
        request = {
                @ApiParam(name = "noteId", value = "帖子ID", dataType = "long")
        })

@Component("collectionDeleteService1.0")
public class CollectionDeleteService extends ReflectApiService {

    @Resource
    private CollectionRepository collectionRepository;

    public Map<String, Object> service(Long currentUserId, Long noteId) {

        Collection c = collectionRepository.findByUserIdAndNoteId(currentUserId, noteId).orElse(null);
        AssertUtil.isTrue(c != null, "收藏记录不存在");
        AssertUtil.isTrue(c.getStatus() == Status.ACTIVE, "收藏记录已删除");

        long now = System.currentTimeMillis() / 1000;
        c.setStatus(Status.DEL);
        c.setUpdateAt(now);
        collectionRepository.save(c);

        return success();
    }
}
