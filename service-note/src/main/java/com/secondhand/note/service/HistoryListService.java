package com.secondhand.note.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.common.output.IDataToMap;
import com.secondhand.common.param.Param;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.History;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Document(desc = "历史列表", module = "note", name = "历史列表", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-01-09",
        request = {
                @ApiParam(name = "page", value = "页数", dataType = "Integer"),
                @ApiParam(name = "limit", value = "一页条数", dataType = "Integer"),
        },
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

@Component("historyListService1.0")
public class HistoryListService extends ReflectApiService {

    @Resource
    private NoteRepository noteRepository;

    public Map<String, Object> service(
            @Param(value = "1", name = "page", required = false) Integer page,
            @Param(value = "10", name = "limit", required = false) Integer limit,
            Long currentUserId) {

        // defaults
        page = (page == null || page < 1) ? 1 : page;
        limit = (limit == null || limit < 1) ? 10 : limit;

        // 1️⃣ total count
        Long total = JdbcQueryBuilder
                .build(History.class)
                .eq("user_id", currentUserId)
                .eq("status", Status.ACTIVE.name())
                .total();

        if (total == 0) {
            return success(Collections.emptyList());
        }

        // 2️⃣ page histories ordered by updateAt desc
        List<History> histories = JdbcQueryBuilder
                .build(History.class)
                .eq("user_id", currentUserId)
                .eq("status", Status.ACTIVE.name())
                .orderBy("update_at", JdbcQueryBuilder.Sort.DESC)
                .limit(page, limit)
                .find(History.class);

        if (histories.isEmpty()) {
            return success(Collections.emptyList());
        }

        // 3️⃣ keep order
        List<Long> noteIdsInOrder = new ArrayList<>();
        for (History h : histories) {
            noteIdsInOrder.add(h.getNoteId());
        }

        // 4️⃣ fetch notes (order not guaranteed)
        List<Note> notes = noteRepository.findByIdIn(noteIdsInOrder);

        // 5️⃣ reorder notes to match history order
        Map<Long, Note> noteMap = notes.stream()
                .collect(Collectors.toMap(Note::getId, Function.identity()));

        List<Note> orderedNotes = new ArrayList<>();
        for (Long noteId : noteIdsInOrder) {
            Note n = noteMap.get(noteId);
            if (n != null) {
                orderedNotes.add(n);
            }
        }

        return success(orderedNotes, (IDataToMap<Note>) (map, source) -> {
            map.put("id", source.getId());
            map.put("userId", source.getUserId());
            map.put("username", source.getUsername());
            map.put("title", source.getTitle());
            map.put("description", source.getDescription());
            map.put("photos", source.getPhotoList());
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
