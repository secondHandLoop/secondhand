package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.geo.GeocodingService;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("noteEditService1.0")
public class NoteEditService extends ReflectApiService {

    @Resource
    private NoteRepository noteRepository;

    @Resource
    private com.secondhand.common.geo.GeocodingService geocodingService;

    @Resource
    private NotePhotoStorageService notePhotoStorageService;

    public Map<String, Object> service(Long currentUserId,
                                       Long noteId,
                                       String title,
                                       String description,
                                       List<String> newPhotoUrls,
                                       List<String> deletePhotoUrls,
                                       String state,
                                       String city,
                                       String address,
                                       String zipcode,
                                       Integer price,
                                       NoteStatus noteStatus,
                                       TopCategory topCategory,
                                       NoteCondition noteCondition,
                                       Boolean delivery) {

        Optional<Note> optional = noteRepository.findById(noteId);
        AssertUtil.isTrue(optional.isPresent(), "帖子不存在");
        Note note = optional.get();

        AssertUtil.isTrue(note.getStatus() == Status.ACTIVE, "帖子已删除");
        AssertUtil.isTrue(currentUserId != null && currentUserId.equals(note.getUserId()), "无权限编辑该帖子");

        boolean locationChanged = false;

        if (title != null) note.setTitle(title);
        if (description != null) note.setDescription(description);

        List<String> currentPhotos = new ArrayList<>(note.getPhotoList());
        if (deletePhotoUrls != null && !deletePhotoUrls.isEmpty()) {
            currentPhotos.removeIf(deletePhotoUrls::contains);
        }
        if (newPhotoUrls != null && !newPhotoUrls.isEmpty()) {
            currentPhotos.addAll(newPhotoUrls);
        }
        note.setPhotoList(new ArrayList<>(new LinkedHashSet<>(currentPhotos)));

        if (state != null && !state.equals(note.getState())) { note.setState(state); locationChanged = true; }
        if (city != null && !city.equals(note.getCity())) { note.setCity(city); locationChanged = true; }
        if (address != null && !address.equals(note.getAddress())) { note.setAddress(address); locationChanged = true; }
        if (zipcode != null && !zipcode.equals(note.getZipcode())) { note.setZipcode(zipcode); locationChanged = true; }

        if (price != null) note.setPrice(price);
        if (noteStatus != null) note.setNoteStatus(noteStatus);
        if (topCategory != null) note.setTopCategory(topCategory);
        if (noteCondition != null) note.setNoteCondition(noteCondition);
        if (delivery != null) note.setDelivery(delivery);

        if (locationChanged) {
            GeocodingService.LatLng ll = geocodingService.geocode(note.getState(), note.getCity(), note.getAddress(), note.getZipcode());
            if (ll != null) {
                note.setLatitude(ll.getLat());
                note.setLongitude(ll.getLng());
            } else {
                // optional: clear if not found
                note.setLatitude(null);
                note.setLongitude(null);
            }
        }

        note.setUpdateAt(System.currentTimeMillis() / 1000);
        noteRepository.save(note);
        if (deletePhotoUrls != null && !deletePhotoUrls.isEmpty()) {
            notePhotoStorageService.deleteByUrls(deletePhotoUrls);
        }
        return success();
    }
}
