package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Document(desc = "增加帖子", module = "note", name = "增加帖子", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2025-11-29",
        request = {
                @ApiParam(name = "username", value = "用户名", dataType = "String"),
                @ApiParam(name = "title", value = "标题", dataType = "String"),
                @ApiParam(name = "description", value = "描述", dataType = "String"),
                @ApiParam(name = "photo", value = "图片", dataType = "String"),
                @ApiParam(name = "state", value = "州", dataType = "String"),
                @ApiParam(name = "city", value = "城市", dataType = "String"),
                @ApiParam(name = "address", value = "地址", dataType = "String"),
                @ApiParam(name = "zipcode", value = "邮编", dataType = "String"),
                @ApiParam(name = "price", value = "价格", dataType = "String"),
                @ApiParam(name = "noteStatus", value = "帖子状态", dataType = "enum"),
                @ApiParam(name = "topCategory", value = "一级类", dataType = "enum"),
                @ApiParam(name = "delivery", value = "是否运输", dataType = "boolean", note = "True: 可运输，False: 自取"),
        })

@Component("noteAddService1.0")
public class NoteAddService extends ReflectApiService {
    @Resource
    private NoteRepository noteRepository;

    public Map<String, Object> service(Long uid,
                                       String username,
                                       String title,
                                       String description,
                                       List<String> photo,
                                       String state,
                                       String city,
                                       String address,
                                       String zipcode,
                                       int price,
                                       NoteStatus noteStatus,
                                       TopCategory topCategory,
                                       boolean delivery) {
        Note note = new Note();
        note.setUserId(uid);
        note.setUsername(username);
        note.setTitle(title);
        note.setDescription(description);
        note.setPhotos(photo);
        note.setState(state);
        note.setCity(city);
        note.setAddress(address);
        note.setZipcode(zipcode);
        note.setPrice(price);
        note.setNoteStatus(noteStatus);
        note.setTopCategory(topCategory);
        note.setDelivery(delivery);

        noteRepository.save(note);
        return success();
    }

}
