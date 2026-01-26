package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.common.output.IDataToMap;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.Collection;
import com.secondhand.note.entity.Note;
import org.springframework.stereotype.Component;
import java.util.*;
import static com.secondhand.common.jdbc.JdbcQueryBuilder.Sort.DESC;

@Document(desc = "收藏列表", module = "note", name = "收藏列表", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-01-09",
        response = {
                @ApiParam(name = "id", value = "帖子id", dataType = "String"),
                @ApiParam(name = "userId", value = "用户id", dataType = "String"),
                @ApiParam(name = "username", value = "用户名", dataType = "String"),
                @ApiParam(name = "title", value = "标题", dataType = "String"),
                @ApiParam(name = "description", value = "描述", dataType = "String"),
                @ApiParam(name = "photos", value = "图片", dataType = "List<String>"),
                @ApiParam(name = "state", value = "州", dataType = "String"),
                @ApiParam(name = "city", value = "城市", dataType = "String"),
                @ApiParam(name = "address", value = "地址", dataType = "String"),
                @ApiParam(name = "zipcode", value = "邮编", dataType = "String"),
                @ApiParam(name = "price", value = "价格", dataType = "String"),
                @ApiParam(name = "noteStatus", value = "帖子状态", dataType = "enum"),
                @ApiParam(name = "topCategory", value = "一级类", dataType = "enum"),
                @ApiParam(name = "delivery", value = "是否运输", dataType = "boolean", note = "True: 可运输，False: 自取"),
        })

@Component("collectionListService1.0")
public class CollectionListService extends ReflectApiService {

    public Map<String, Object> service(Long uid) {
        
        List<Collection> collections = JdbcQueryBuilder
                .build(Collection.class)
                .eq("user_id", uid)
                .eq("status", Status.ACTIVE.name())
                .orderBy("update_at", DESC)
                .find(Collection.class);

        if (collections.isEmpty()) {
            return success(Collections.emptyList());
        }

        List<Long> noteIdsInOrder = new ArrayList<>();
        for (Collection c : collections) {
            noteIdsInOrder.add(c.getNoteId());
        }

        List<Note> notes = JdbcQueryBuilder
                .build(Note.class)
                .in("id", noteIdsInOrder)
                .eq("status", Status.ACTIVE.name())
                .find(Note.class);

        Map<Long, Note> noteMap = new HashMap<>();
        for (Note note : notes) {
            noteMap.put(note.getId(), note);
        }

        List<Note> orderedNotes = new ArrayList<>();
        for (Long noteId : noteIdsInOrder) {
            Note note = noteMap.get(noteId);
            if (note != null) {
                orderedNotes.add(note);
            }
        }

        return success(orderedNotes, (IDataToMap<Note>) (map, source) -> {
            map.put("id", source.getId());
            map.put("userId", source.getUserId());
            map.put("username", source.getUsername());
            map.put("title", source.getTitle());
            map.put("description", source.getDescription());
            map.put("photos", source.getPhotos());
            map.put("state", source.getState());
            map.put("city", source.getCity());
            map.put("address", source.getAddress());
            map.put("zipcode", source.getZipcode());
            map.put("price", source.getPrice());
            map.put("noteStatus", source.getNoteStatus());
            map.put("topCategory", source.getTopCategory());
            map.put("delivery", source.isDelivery());
            map.put("updateAt", source.getUpdateAt());
        });
    }
}
