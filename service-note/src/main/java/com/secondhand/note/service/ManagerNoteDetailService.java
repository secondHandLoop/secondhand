package com.secondhand.note.service;

import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ManagerNoteDetailService extends ApiResponseHelper {

    private final NoteRepository noteRepository;

    public ManagerNoteDetailService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Map<String, Object> service(Long noteId) {
        AssertUtil.notNull(noteId, "帖子ID不能为空");

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", note.getId());
        body.put("userId", note.getUserId());
        body.put("username", note.getUsername());
        body.put("title", note.getTitle());
        body.put("description", note.getDescription());
        body.put("photos", note.getPhotoList());
        body.put("state", note.getState());
        body.put("city", note.getCity());
        body.put("address", note.getAddress());
        body.put("zipcode", note.getZipcode());
        body.put("latitude", note.getLatitude());
        body.put("longitude", note.getLongitude());
        body.put("price", note.getPrice());
        body.put("score", note.getScore());
        body.put("comment", note.getComment());
        body.put("noteStatus", note.getNoteStatus());
        body.put("topCategory", note.getTopCategory());
        body.put("delivery", note.isDelivery());
        body.put("status", note.getStatus());
        body.put("createAt", note.getCreateAt());
        body.put("updateAt", note.getUpdateAt());

        return success(body);
    }
}
