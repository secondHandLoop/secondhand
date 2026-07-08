package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.note.dto.NoteResponseMapper;
import com.secondhand.note.entity.History;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import com.secondhand.note.openapi.schema.NoteListItemResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lists a user's browsing history and resolves it back to note summaries.
 */
@Service
public class HistoryListService {

    @Resource
    private NoteRepository noteRepository;

    /**
     * Returns one page of recently viewed notes in history order.
     */
    public ApiResponse<List<NoteListItemResponse>> listHistories(
            Integer page,
            Integer limit,
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
            return ApiResponse.success(Collections.emptyList());
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
            return ApiResponse.success(Collections.emptyList());
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

        List<NoteListItemResponse> items = new ArrayList<>(orderedNotes.size());
        for (Note note : orderedNotes) {
            items.add(NoteResponseMapper.toNoteListItem(note));
        }
        return ApiResponse.success(items);
    }
}
