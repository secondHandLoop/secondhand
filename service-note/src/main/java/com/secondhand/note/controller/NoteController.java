package com.secondhand.note.controller;

import com.secondhand.common.controller.RequestUserIdResolver;
import com.secondhand.note.dto.HistoryDeleteRequest;
import com.secondhand.note.dto.ManagerNoteDetailResponseBody;
import com.secondhand.note.dto.ManagerNoteListItemResponse;
import com.secondhand.note.dto.ManagerNoteListQuery;
import com.secondhand.note.dto.NoteCreateRequest;
import com.secondhand.note.dto.NoteDetailResponseBody;
import com.secondhand.note.dto.NoteEditRequest;
import com.secondhand.note.dto.NoteIdRequest;
import com.secondhand.note.dto.PaginationQuery;
import com.secondhand.note.dto.UploadMediaResponse;
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
import com.secondhand.note.service.NotePhotoStorageService;
import com.secondhand.note.openapi.schema.ApiErrorResponse;
import com.secondhand.note.openapi.schema.NoteListQuery;
import com.secondhand.note.openapi.schema.NoteListItemResponse;
import com.secondhand.note.openapi.schema.NoteListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/note")
@Tag(name = "Notes", description = "Note listing, details, posting, collections, and history APIs.")
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

    @Operation(
            summary = "List notes",
            description = "Returns notes visible to the caller. Supports normal recency sorting, personalized explore ranking, nearby sorting, and a my-notes mode for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notes returned successfully.",
                    content = @Content(schema = @Schema(implementation = NoteListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request parameters are invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "The service failed to process the request.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/notes")
    public NoteListResponse listNotes(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                      @ParameterObject NoteListQuery query) {
        Long currentUserId = userIdResolver.resolveOptionalUserId(authorizationHeader);
        return noteListService.listNotes(query.getPage(), query.getLimit(), currentUserId, query.getKeyword(),
                query.getTopCategory(), query.getDelivery(), query.getMyNotes(), query.getExplore(),
                query.getInterestCategories(), query.getNearby(), query.getUserLat(), query.getUserLng(),
                query.getUserCity(), query.getUserState(), query.getUserZipcode());
    }

    @Operation(
            summary = "Get note detail",
            description = "Returns the full detail for a note. Unpublished notes are visible only to their owner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note detail returned successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The note id is invalid or inaccessible.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "The service failed to process the request.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/notes/{noteId}")
    public com.secondhand.note.dto.ApiResponse<NoteDetailResponseBody> noteDetail(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                                                                  @Parameter(description = "Note id to load.") @PathVariable Long noteId) {
        Long currentUserId = userIdResolver.resolveOptionalUserId(authorizationHeader);
        return noteDetailService.getNoteDetail(currentUserId, noteId);
    }

    @Operation(
            summary = "List notes for managers",
            description = "Returns a filtered, paginated note list for manager moderation and review flows."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manager note list returned successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Only managers can access this endpoint.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/manage/notes")
    public com.secondhand.note.dto.ApiResponse<List<ManagerNoteListItemResponse>> manageNotes(@RequestHeader("Authorization") String authorizationHeader,
                                                                                              @ParameterObject ManagerNoteListQuery query) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerNoteListService.listNotesForManager(query.getPage(), query.getLimit(), query.getKeyword(),
                query.getUserId(), query.getNoteStatus(), query.getTopCategory(), query.getDelivery(), query.getStatus());
    }

    @Operation(
            summary = "Get note detail for managers",
            description = "Returns the full manager-facing note detail, including administrative status fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manager note detail returned successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Only managers can access this endpoint.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/manage/notes/{noteId}")
    public com.secondhand.note.dto.ApiResponse<ManagerNoteDetailResponseBody> manageNoteDetail(@RequestHeader("Authorization") String authorizationHeader,
                                                                                                @Parameter(description = "Note id to inspect.") @PathVariable Long noteId) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerNoteDetailService.getNoteDetailForManager(noteId);
    }

    @Operation(
            summary = "Create a note",
            description = "Creates a new note for the authenticated user. Uploaded photos are stored first and their URLs are attached to the note."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note created successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The note payload is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public com.secondhand.note.dto.ApiResponse<Void> addNote(@RequestHeader("Authorization") String authorizationHeader,
                                                             @ModelAttribute NoteCreateRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        List<String> photoUrls = notePhotoStorageService.uploadPhotos(request.getPhotos(), currentUserId);
        return noteAddService.addNote(currentUserId, request.getUsername(), request.getTitle(), request.getDescription(),
                photoUrls, request.getState(), request.getCity(), request.getAddress(), request.getZipcode(),
                request.getPrice(), request.getNoteStatus(), request.getTopCategory(), request.getNoteCondition(),
                request.isDelivery());
    }

    @Operation(
            summary = "Edit a note",
            description = "Updates a note owned by the authenticated user. New photos are uploaded before the update is saved."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note updated successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The note update is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping(value = "/notes/{noteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public com.secondhand.note.dto.ApiResponse<Void> editNote(@RequestHeader("Authorization") String authorizationHeader,
                                                              @Parameter(description = "Note id to update.") @PathVariable Long noteId,
                                                              @ModelAttribute NoteEditRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        List<String> newPhotoUrls = notePhotoStorageService.uploadPhotos(request.getNewPhotos(), currentUserId);
        return noteEditService.editNote(currentUserId, noteId, request.getTitle(), request.getDescription(),
                newPhotoUrls, request.getDeletePhotos(), request.getState(), request.getCity(), request.getAddress(),
                request.getZipcode(), request.getPrice(), request.getNoteStatus(), request.getTopCategory(),
                request.getNoteCondition(), request.getDelivery());
    }

    @Operation(
            summary = "Delete a note",
            description = "Soft-deletes a note owned by the authenticated user and removes its stored photos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note deleted successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The note cannot be deleted.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/notes/{noteId}")
    public com.secondhand.note.dto.ApiResponse<Void> deleteNote(@RequestHeader("Authorization") String authorizationHeader,
                                                                @Parameter(description = "Note id to delete.") @PathVariable Long noteId) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return noteDeleteService.deleteNote(currentUserId, noteId);
    }

    @Operation(
            summary = "Upload note media",
            description = "Uploads a single media file for note editing flows and returns the public URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media uploaded successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The media upload is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadMediaResponse uploadMedia(@RequestHeader("Authorization") String authorizationHeader,
                                           @Parameter(description = "Media file to upload.") @org.springframework.web.bind.annotation.RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        List<String> urls = notePhotoStorageService.uploadPhotos(Collections.singletonList(file), currentUserId);
        UploadMediaResponse result = new UploadMediaResponse();
        result.setUrl(urls.isEmpty() ? "" : urls.get(0));
        return result;
    }

    @Operation(
            summary = "List collections",
            description = "Returns the authenticated user's collected notes in collection order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collections returned successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/collections")
    public com.secondhand.note.dto.ApiResponse<List<NoteListItemResponse>> listCollections(@RequestHeader("Authorization") String authorizationHeader,
                                                                                           @ParameterObject PaginationQuery query) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return collectionListService.listCollections(query.getPage(), query.getLimit(), currentUserId);
    }

    @Operation(
            summary = "Add a collection",
            description = "Adds a note to the authenticated user's collection list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collection added successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The collection request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/collections")
    public com.secondhand.note.dto.ApiResponse<Void> addCollection(@RequestHeader("Authorization") String authorizationHeader,
                                                                   @org.springframework.web.bind.annotation.RequestBody NoteIdRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return collectionAddService.addCollection(currentUserId, request.getNoteId());
    }

    @Operation(
            summary = "Delete a collection",
            description = "Removes a note from the authenticated user's collection list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collection removed successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The collection request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/collections/{noteId}")
    public com.secondhand.note.dto.ApiResponse<Void> deleteCollection(@RequestHeader("Authorization") String authorizationHeader,
                                                                      @Parameter(description = "Note id to remove from collections.") @PathVariable Long noteId) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return collectionDeleteService.deleteCollection(currentUserId, noteId);
    }

    @Operation(
            summary = "List browsing history",
            description = "Returns the authenticated user's browsing history ordered by most recent visit."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Browsing history returned successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/histories")
    public com.secondhand.note.dto.ApiResponse<List<NoteListItemResponse>> listHistories(@RequestHeader("Authorization") String authorizationHeader,
                                                                                         @ParameterObject PaginationQuery query) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return historyListService.listHistories(query.getPage(), query.getLimit(), currentUserId);
    }

    @Operation(
            summary = "Add a history record",
            description = "Records that the authenticated user viewed the specified note."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History record saved successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The history request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/histories")
    public com.secondhand.note.dto.ApiResponse<Void> addHistory(@RequestHeader("Authorization") String authorizationHeader,
                                                                @org.springframework.web.bind.annotation.RequestBody NoteIdRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return historyAddService.addHistory(currentUserId, request.getNoteId());
    }

    @Operation(
            summary = "Delete history records",
            description = "Deletes one or more browsing history records for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History records deleted successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "The history delete request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/histories")
    public com.secondhand.note.dto.ApiResponse<Void> deleteHistories(@RequestHeader("Authorization") String authorizationHeader,
                                                                     @org.springframework.web.bind.annotation.RequestBody HistoryDeleteRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return historyDeleteService.deleteHistories(currentUserId, request.getNoteIds());
    }
}
