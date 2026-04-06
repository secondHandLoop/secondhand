package com.secondhand.note.dto;

import com.secondhand.note.entity.Note;
import com.secondhand.note.openapi.schema.NoteListItemResponse;

public final class NoteResponseMapper {

    private NoteResponseMapper() {
    }

    public static NoteListItemResponse toNoteListItem(Note source) {
        NoteListItemResponse item = new NoteListItemResponse();
        item.setId(source.getId());
        item.setUserId(source.getUserId());
        item.setUsername(source.getUsername());
        item.setTitle(source.getTitle());
        item.setDescription(source.getDescription());
        item.setPhotos(source.getPhotoList());
        item.setState(source.getState());
        item.setCity(source.getCity());
        item.setAddress(source.getAddress());
        item.setZipcode(source.getZipcode());
        item.setLatitude(source.getLatitude());
        item.setLongitude(source.getLongitude());
        item.setPrice(source.getPrice());
        item.setTopCategory(source.getTopCategory());
        item.setNoteCondition(source.getNoteCondition());
        item.setDelivery(source.isDelivery());
        item.setUpdateAt(source.getUpdateAt());
        item.setNoteStatus(source.getNoteStatus());
        return item;
    }

    public static NoteDetailResponseBody toNoteDetail(Note source, boolean owner, boolean collected) {
        NoteDetailResponseBody body = new NoteDetailResponseBody();
        body.setId(source.getId());
        body.setUserId(source.getUserId());
        body.setUsername(source.getUsername());
        body.setTitle(source.getTitle());
        body.setDescription(source.getDescription());
        body.setPhotos(source.getPhotoList());
        body.setState(source.getState());
        body.setCity(source.getCity());
        body.setAddress(source.getAddress());
        body.setZipcode(source.getZipcode());
        body.setLatitude(source.getLatitude());
        body.setLongitude(source.getLongitude());
        body.setPrice(source.getPrice());
        body.setScore(source.getScore());
        body.setComment(source.getComment());
        body.setNoteStatus(source.getNoteStatus());
        body.setTopCategory(source.getTopCategory());
        body.setNoteCondition(source.getNoteCondition());
        body.setDelivery(source.isDelivery());
        body.setCreateAt(source.getCreateAt());
        body.setUpdateAt(source.getUpdateAt());
        body.setIsOwner(owner);
        body.setIsCollected(collected);
        return body;
    }

    public static ManagerNoteDetailResponseBody toManagerNoteDetail(Note source) {
        ManagerNoteDetailResponseBody body = new ManagerNoteDetailResponseBody();
        body.setId(source.getId());
        body.setUserId(source.getUserId());
        body.setUsername(source.getUsername());
        body.setTitle(source.getTitle());
        body.setDescription(source.getDescription());
        body.setPhotos(source.getPhotoList());
        body.setState(source.getState());
        body.setCity(source.getCity());
        body.setAddress(source.getAddress());
        body.setZipcode(source.getZipcode());
        body.setLatitude(source.getLatitude());
        body.setLongitude(source.getLongitude());
        body.setPrice(source.getPrice());
        body.setScore(source.getScore());
        body.setComment(source.getComment());
        body.setNoteStatus(source.getNoteStatus());
        body.setTopCategory(source.getTopCategory());
        body.setNoteCondition(source.getNoteCondition());
        body.setDelivery(source.isDelivery());
        body.setStatus(source.getStatus());
        body.setCreateAt(source.getCreateAt());
        body.setUpdateAt(source.getUpdateAt());
        return body;
    }

    public static ManagerNoteListItemResponse toManagerNoteListItem(Note source) {
        ManagerNoteListItemResponse item = new ManagerNoteListItemResponse();
        item.setId(source.getId());
        item.setUserId(source.getUserId());
        item.setUsername(source.getUsername());
        item.setTitle(source.getTitle());
        item.setDescription(source.getDescription());
        item.setPhotos(source.getPhotoList());
        item.setState(source.getState());
        item.setCity(source.getCity());
        item.setZipcode(source.getZipcode());
        item.setPrice(source.getPrice());
        item.setNoteStatus(source.getNoteStatus());
        item.setTopCategory(source.getTopCategory());
        item.setDelivery(source.isDelivery());
        item.setStatus(source.getStatus());
        item.setCreateAt(source.getCreateAt());
        item.setUpdateAt(source.getUpdateAt());
        return item;
    }
}
