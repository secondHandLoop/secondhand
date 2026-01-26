package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.common.output.IDataToMap;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.History;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.HistoryRepository;
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
    private HistoryRepository historyRepository;

    @Resource
    private NoteRepository noteRepository;

    public Map<String, Object> service(Long uid) {

        List<History> histories = JdbcQueryBuilder
                .build(History.class)
                .eq("user_id", uid)
                .eq("status", Status.ACTIVE.name())
                .orderBy("update_at", JdbcQueryBuilder.Sort.DESC)
                .find(History.class);

        if (histories.isEmpty()) {
            return success(Collections.emptyList());
        }

        List<Long> noteIdsInOrder = new ArrayList<>();
        for (History h : histories) {
            noteIdsInOrder.add(h.getNoteId());
        }

        List<Note> notes = noteRepository.findByIdIn(noteIdsInOrder);

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