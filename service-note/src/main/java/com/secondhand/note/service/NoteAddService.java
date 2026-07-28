package com.secondhand.note.service;

import com.secondhand.common.output.ApiResponse;
import com.secondhand.common.geo.GeocodingService;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creates new notes and enriches them with geocoded location data when possible.
 */
@Service
public class NoteAddService {
    @Resource
    private NoteRepository noteRepository;

    @Resource
    private com.secondhand.common.geo.GeocodingService geocodingService;

    @Resource
    private NoteEmbeddingService noteEmbeddingService;

    /**
     * Persists a new note owned by the current user.
     */
    public ApiResponse<Void> addNote(Long currentUserId,
                                     String username,
                                     String title,
                                     String description,
                                     List<String> photoUrls,
                                     String state,
                                     String city,
                                     String address,
                                     String zipcode,
                                     int price,
                                     NoteStatus noteStatus,
                                     TopCategory topCategory,
                                     NoteCondition noteCondition,
                                     boolean delivery,
                                     String attributes) {

        Note note = new Note();
        note.setUserId(currentUserId);
        note.setUsername(username);
        note.setTitle(title);
        note.setDescription(description);
        note.setPhotoList(photoUrls);
        note.setState(state);
        note.setCity(city);
        note.setAddress(address);
        note.setZipcode(zipcode);
        note.setPrice(price);
        note.setNoteStatus(noteStatus);
        note.setTopCategory(topCategory);
        note.setNoteCondition(noteCondition);
        note.setDelivery(delivery);
        note.setAttributes(attributes);

        // geocode
        GeocodingService.LatLng ll = geocodingService.geocode(state, city, address, zipcode);
        if (ll != null) {
            note.setLatitude(ll.getLat());
            note.setLongitude(ll.getLng());
        }

        noteRepository.save(note);
        noteEmbeddingService.embedNoteAsync(note);
        return ApiResponse.success();
    }
}
