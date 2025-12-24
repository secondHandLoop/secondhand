package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.Status;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Document(desc = "增加帖子", module = "note", name = "增加帖子", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2025-11-29",
        request = {
                @ApiParam(name = "username", value = "user", dataType = "User", note = "packing using User"),
                @ApiParam(name = "title", value = "邮箱", dataType = "String"),
                @ApiParam(name = "description", value = "密码", dataType = "String"),
                @ApiParam(name = "photo", value = "姓名", dataType = "String"),
                @ApiParam(name = "state", value = "姓名", dataType = "String"),
                @ApiParam(name = "city", value = "姓名", dataType = "String"),
                @ApiParam(name = "address", value = "姓名", dataType = "String"),
                @ApiParam(name = "zipcode", value = "姓名", dataType = "String"),
                @ApiParam(name = "price", value = "姓名", dataType = "String"),
                @ApiParam(name = "status", value = "姓名", dataType = "String"),
                @ApiParam(name = "topCategory", value = "姓名", dataType = "String"),
                @ApiParam(name = "delivery", value = "姓名", dataType = "String"),
        },
        response = {
                @ApiParam(name = "id", value = "用户ID", dataType = "String"),
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "name", value = "姓名", dataType = "String")
        })

@Component("noteAddService1.0")
public class NoteAddService extends ReflectApiService {
    @Resource
    private NoteRepository noteRepository;
    public Map<String, Object>  service(long uid, String username, String title, String description, List<String> photo,
                                        String state, String city, String address, String zipcode, int price, Status status,
                                        TopCategory topCategory, boolean delivery) {
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
        note.setStatus(status);
        note.setTopCategory(topCategory);
        note.setDelivery(delivery);

        noteRepository.save(note);
        return success();
    }

}
