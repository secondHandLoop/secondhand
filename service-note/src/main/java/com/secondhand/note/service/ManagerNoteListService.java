package com.secondhand.note.service;

import com.secondhand.common.entity.Status;
import com.secondhand.note.dto.ApiResponse;
import com.secondhand.note.dto.ManagerNoteListItemResponse;
import com.secondhand.note.dto.NoteResponseMapper;
import com.secondhand.note.dto.PageInfoResponse;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.repository.NoteRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the manager note list with administrative filters and pagination metadata.
 */
@Service
public class ManagerNoteListService {

    private final NoteRepository noteRepository;

    public ManagerNoteListService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Returns a filtered page of notes for manager review screens.
     */
    public ApiResponse<List<ManagerNoteListItemResponse>> listNotesForManager(Integer page,
                                                                              Integer limit,
                                                                              String keyword,
                                                                              Long userId,
                                                                              NoteStatus noteStatus,
                                                                              TopCategory topCategory,
                                                                              Boolean delivery,
                                                                              String status) {
        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (limit == null || limit < 1) ? 20 : limit;

        Specification<Note> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), likeValue),
                        cb.like(cb.lower(root.get("title")), likeValue),
                        cb.like(cb.lower(root.get("description")), likeValue),
                        cb.like(cb.lower(root.get("city")), likeValue),
                        cb.like(cb.lower(root.get("state")), likeValue)
                ));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (noteStatus != null) {
                predicates.add(cb.equal(root.get("noteStatus"), noteStatus));
            }
            if (topCategory != null) {
                predicates.add(cb.equal(root.get("topCategory"), topCategory));
            }
            if (delivery != null) {
                predicates.add(cb.equal(root.get("delivery"), delivery));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), Status.valueOf(status.trim().toUpperCase(Locale.ROOT))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Note> result = noteRepository.findAll(
                specification,
                PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateAt"))
        );

        List<ManagerNoteListItemResponse> body = new ArrayList<>();
        for (Note note : result.getContent()) {
            body.add(NoteResponseMapper.toManagerNoteListItem(note));
        }

        PageInfoResponse pageInfo = new PageInfoResponse();
        pageInfo.setPage(pageNo);
        pageInfo.setLimit(pageSize);
        pageInfo.setTotal(result.getTotalElements());

        return ApiResponse.success(body, pageInfo);
    }
}
