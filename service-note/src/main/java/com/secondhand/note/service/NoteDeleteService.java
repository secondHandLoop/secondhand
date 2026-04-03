package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.Note;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("noteDeleteService1.0")
public class NoteDeleteService extends ReflectApiService {

    @Resource
    NoteRepository noteRepository;

    @Resource
    NotePhotoStorageService notePhotoStorageService;

    public Map<String, Object> service(Long currentUserId,
                                       Long noteId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        AssertUtil.isTrue(note.getUserId() == currentUserId, "无权限删除该帖子");

        AssertUtil.isTrue(note.getStatus() != Status.DEL, "帖子已删除");

        note.setStatus(Status.DEL);
        noteRepository.save(note);
        notePhotoStorageService.deleteByUrls(note.getPhotoList());

        return success();
    }
}
