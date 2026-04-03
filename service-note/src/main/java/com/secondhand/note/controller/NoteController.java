package com.secondhand.note.controller;

import com.secondhand.common.controller.RequestUserIdResolver;
import com.secondhand.note.service.CollectionAddService;
import com.secondhand.note.service.CollectionDeleteService;
import com.secondhand.note.service.CollectionListService;
import com.secondhand.note.service.HistoryAddService;
import com.secondhand.note.service.HistoryDeleteService;
import com.secondhand.note.service.HistoryListService;
import com.secondhand.note.service.NoteAddService;
import com.secondhand.note.service.NoteDeleteService;
import com.secondhand.note.service.NoteDetailService;
import com.secondhand.note.service.NoteEditService;
import com.secondhand.note.service.NoteListService;
import com.secondhand.note.service.ManagerNoteDetailService;
import com.secondhand.note.service.ManagerNoteListService;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.service.NotePhotoStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/note")
public class NoteController {

    private final RequestUserIdResolver userIdResolver;
    private final NoteAddService noteAddService;
    private final NoteEditService noteEditService;
    private final NoteDeleteService noteDeleteService;
    private final NoteListService noteListService;
    private final NoteDetailService noteDetailService;
    private final ManagerNoteListService managerNoteListService;
    private final ManagerNoteDetailService managerNoteDetailService;
    private final NotePhotoStorageService notePhotoStorageService;
    private final CollectionAddService collectionAddService;
    private final CollectionDeleteService collectionDeleteService;
    private final CollectionListService collectionListService;
    private final HistoryAddService historyAddService;
    private final HistoryDeleteService historyDeleteService;
    private final HistoryListService historyListService;

    public NoteController(RequestUserIdResolver userIdResolver,
                          NoteAddService noteAddService,
                          NoteEditService noteEditService,
                          NoteDeleteService noteDeleteService,
                          NoteListService noteListService,
                          NoteDetailService noteDetailService,
                          ManagerNoteListService managerNoteListService,
                          ManagerNoteDetailService managerNoteDetailService,
                          NotePhotoStorageService notePhotoStorageService,
                          CollectionAddService collectionAddService,
                          CollectionDeleteService collectionDeleteService,
                          CollectionListService collectionListService,
                          HistoryAddService historyAddService,
                          HistoryDeleteService historyDeleteService,
                          HistoryListService historyListService) {
        this.userIdResolver = userIdResolver;
        this.noteAddService = noteAddService;
        this.noteEditService = noteEditService;
        this.noteDeleteService = noteDeleteService;
        this.noteListService = noteListService;
        this.noteDetailService = noteDetailService;
        this.managerNoteListService = managerNoteListService;
        this.managerNoteDetailService = managerNoteDetailService;
        this.notePhotoStorageService = notePhotoStorageService;
        this.collectionAddService = collectionAddService;
        this.collectionDeleteService = collectionDeleteService;
        this.collectionListService = collectionListService;
        this.historyAddService = historyAddService;
        this.historyDeleteService = historyDeleteService;
        this.historyListService = historyListService;
    }

    @GetMapping("/notes")
    public Map<String, Object> listNotes(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer limit,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) TopCategory topCategory,
                                         @RequestParam(required = false) Boolean delivery,
                                         @RequestParam(required = false) Boolean myNotes,
                                         @RequestParam(required = false) Boolean explore,
                                         @RequestParam(required = false) List<TopCategory> interestCategories,
                                         @RequestParam(required = false) Boolean nearby,
                                         @RequestParam(required = false) Double userLat,
                                         @RequestParam(required = false) Double userLng,
                                         @RequestParam(required = false) String userCity,
                                         @RequestParam(required = false) String userState,
                                         @RequestParam(required = false) String userZipcode) {
        Long currentUserId = userIdResolver.resolveOptionalUserId(authorizationHeader);
        return noteListService.service(page, limit, currentUserId, keyword, topCategory, delivery, myNotes,
                explore, interestCategories, nearby, userLat, userLng, userCity, userState, userZipcode);
    }

    @GetMapping("/notes/{noteId}")
    public Map<String, Object> noteDetail(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                          @PathVariable Long noteId) {
        Long currentUserId = userIdResolver.resolveOptionalUserId(authorizationHeader);
        return noteDetailService.service(currentUserId, noteId);
    }

    @GetMapping("/manage/notes")
    public Map<String, Object> manageNotes(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer limit,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) NoteStatus noteStatus,
                                           @RequestParam(required = false) TopCategory topCategory,
                                           @RequestParam(required = false) Boolean delivery,
                                           @RequestParam(required = false) String status) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerNoteListService.service(page, limit, keyword, userId, noteStatus, topCategory, delivery, status);
    }

    @GetMapping("/manage/notes/{noteId}")
    public Map<String, Object> manageNoteDetail(@RequestHeader("Authorization") String authorizationHeader,
                                                @PathVariable Long noteId) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerNoteDetailService.service(noteId);
    }

    @PostMapping(value = "/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> addNote(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestParam String username,
                                       @RequestParam String title,
                                       @RequestParam String description,
                                       @RequestParam String state,
                                       @RequestParam String city,
                                       @RequestParam String address,
                                       @RequestParam String zipcode,
                                       @RequestParam int price,
                                       @RequestParam NoteStatus noteStatus,
                                       @RequestParam TopCategory topCategory,
                                       @RequestParam(required = false) NoteCondition noteCondition,
                                       @RequestParam boolean delivery,
                                       @RequestPart(name = "photos", required = false) List<MultipartFile> photos) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        List<String> photoUrls = notePhotoStorageService.uploadPhotos(photos, currentUserId);
        return noteAddService.service(currentUserId, username, title, description, photoUrls, state, city, address, zipcode,
                price, noteStatus, topCategory, noteCondition, delivery);
    }

    @PutMapping(value = "/notes/{noteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> editNote(@RequestHeader("Authorization") String authorizationHeader,
                                        @PathVariable Long noteId,
                                        @RequestParam(required = false) String title,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false) String state,
                                        @RequestParam(required = false) String city,
                                        @RequestParam(required = false) String address,
                                        @RequestParam(required = false) String zipcode,
                                        @RequestParam(required = false) Integer price,
                                        @RequestParam(required = false) NoteStatus noteStatus,
                                        @RequestParam(required = false) TopCategory topCategory,
                                        @RequestParam(required = false) NoteCondition noteCondition,
                                        @RequestParam(required = false) Boolean delivery,
                                        @RequestParam(required = false) List<String> deletePhotos,
                                        @RequestPart(name = "newPhotos", required = false) List<MultipartFile> newPhotos) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        List<String> newPhotoUrls = notePhotoStorageService.uploadPhotos(newPhotos, currentUserId);
        return noteEditService.service(currentUserId, noteId, title, description, newPhotoUrls, deletePhotos,
                state, city, address, zipcode, price, noteStatus, topCategory, noteCondition, delivery);
    }

    @DeleteMapping("/notes/{noteId}")
    public Map<String, Object> deleteNote(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable Long noteId) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return noteDeleteService.service(currentUserId, noteId);
    }

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadMedia(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestPart("file") MultipartFile file) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        List<String> urls = notePhotoStorageService.uploadPhotos(Collections.singletonList(file), currentUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("url", urls.isEmpty() ? "" : urls.get(0));
        return result;
    }

    @GetMapping("/collections")
    public Map<String, Object> listCollections(@RequestHeader("Authorization") String authorizationHeader,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer limit) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return collectionListService.service(page, limit, currentUserId);
    }

    @PostMapping("/collections")
    public Map<String, Object> addCollection(@RequestHeader("Authorization") String authorizationHeader,
                                             @org.springframework.web.bind.annotation.RequestBody NoteIdRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return collectionAddService.service(currentUserId, request.getNoteId());
    }

    @DeleteMapping("/collections/{noteId}")
    public Map<String, Object> deleteCollection(@RequestHeader("Authorization") String authorizationHeader,
                                                @PathVariable Long noteId) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return collectionDeleteService.service(currentUserId, noteId);
    }

    @GetMapping("/histories")
    public Map<String, Object> listHistories(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer limit) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return historyListService.service(page, limit, currentUserId);
    }

    @PostMapping("/histories")
    public Map<String, Object> addHistory(@RequestHeader("Authorization") String authorizationHeader,
                                          @org.springframework.web.bind.annotation.RequestBody NoteIdRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return historyAddService.service(currentUserId, request.getNoteId());
    }

    @DeleteMapping("/histories")
    public Map<String, Object> deleteHistories(@RequestHeader("Authorization") String authorizationHeader,
                                               @org.springframework.web.bind.annotation.RequestBody HistoryDeleteRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return historyDeleteService.service(currentUserId, request.getNoteIds());
    }

    public static class NoteIdRequest {
        private Long noteId;

        public Long getNoteId() {
            return noteId;
        }

        public void setNoteId(Long noteId) {
            this.noteId = noteId;
        }
    }

    public static class HistoryDeleteRequest {
        private List<Long> noteIds;

        public List<Long> getNoteIds() {
            return noteIds;
        }

        public void setNoteIds(List<Long> noteIds) {
            this.noteIds = noteIds;
        }
    }
}
