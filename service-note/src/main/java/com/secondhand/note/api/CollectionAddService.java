package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.Collection;
import com.secondhand.note.repository.CollectionRepository;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Document(desc = "增加收藏", module = "note", name = "增加收藏", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-01-09",
        request = {
                @ApiParam(name = "noteId", value = "帖子ID", dataType = "long")
        })

@Component("collectionAddService1.0")
public class CollectionAddService extends ReflectApiService {

    @Resource
    private CollectionRepository collectionRepository;

    @Resource
    private NoteRepository noteRepository;

    public Map<String, Object> service(Long uid, Long noteId) {

        AssertUtil.isTrue(noteRepository.existsById(noteId), "帖子不存在");

        Collection c = collectionRepository.findByUserIdAndNoteId(uid, noteId).orElse(null);

        long now = System.currentTimeMillis() / 1000;

        if (c == null) {
            c = Collection.builder()
                    .userId(uid)
                    .noteId(noteId)
                    .build();
            collectionRepository.save(c);
            return success();
        }

        if (c.getStatus() == Status.ACTIVE) {
            AssertUtil.isTrue(false, "已收藏过该帖子");
        }

        c.setStatus(Status.ACTIVE);
        c.setUpdateAt(now);
        collectionRepository.save(c);

        return success();
    }
}
