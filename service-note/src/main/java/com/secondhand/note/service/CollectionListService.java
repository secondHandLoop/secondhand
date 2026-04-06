package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.note.dto.ApiResponse;
import com.secondhand.note.dto.NoteResponseMapper;
import com.secondhand.note.entity.Collection;
import com.secondhand.note.entity.Note;
import com.secondhand.note.openapi.schema.NoteListItemResponse;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.secondhand.common.jdbc.JdbcQueryBuilder.Sort.DESC;

/**
 * Lists the notes a user has collected while preserving collection order.
 */
@Service
public class CollectionListService {

    /**
     * Returns one page of collected notes ordered by the collection update time.
     */
    public ApiResponse<List<NoteListItemResponse>> listCollections(
            Integer page,
            Integer limit,
            Long currentUserId) {

        page = (page == null || page < 1) ? 1 : page;
        limit = (limit == null || limit < 1) ? 10 : limit;

        Long total = JdbcQueryBuilder
                .build(Collection.class)
                .eq("user_id", currentUserId)
                .eq("status", Status.ACTIVE.name())
                .total();

        if (total == 0) {
            return ApiResponse.success(Collections.emptyList());
        }

        List<Collection> collections = JdbcQueryBuilder
                .build(Collection.class)
                .eq("user_id", currentUserId)
                .eq("status", Status.ACTIVE.name())
                .orderBy("update_at", DESC)
                .limit(page, limit)
                .find(Collection.class);

        if (collections.isEmpty()) {
            return ApiResponse.success(Collections.emptyList());
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

        List<NoteListItemResponse> items = new ArrayList<>(orderedNotes.size());
        for (Note note : orderedNotes) {
            items.add(NoteResponseMapper.toNoteListItem(note));
        }
        return ApiResponse.success(items);
    }
}
