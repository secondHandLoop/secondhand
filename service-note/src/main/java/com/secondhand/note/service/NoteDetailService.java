package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.repository.CollectionRepository;
import com.secondhand.note.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NoteDetailService extends ApiResponseHelper {

    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;

    public NoteDetailService(NoteRepository noteRepository,
                             CollectionRepository collectionRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
    }

    public Map<String, Object> service(Long currentUserId, Long noteId) {
        AssertUtil.notNull(noteId, "帖子ID不能为空");

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        AssertUtil.isTrue(note.getStatus() == Status.ACTIVE, "帖子已删除");

        boolean owner = currentUserId != null && currentUserId.equals(note.getUserId());
        boolean visible = note.getNoteStatus() == NoteStatus.PUBLISHED || owner;
        AssertUtil.isTrue(visible, "无权限查看该帖子");

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
        body.put("noteCondition", note.getNoteCondition());
        body.put("delivery", note.isDelivery());
        body.put("createAt", note.getCreateAt());
        body.put("updateAt", note.getUpdateAt());
        body.put("isOwner", owner);

        boolean collected = false;
        if (currentUserId != null) {
            collected = collectionRepository.findByUserIdAndNoteId(currentUserId, noteId)
                    .map(collection -> collection.getStatus() == Status.ACTIVE)
                    .orElse(false);
        }
        body.put("isCollected", collected);

        return success(body);
    }
}
