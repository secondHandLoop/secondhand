package com.secondhand.note.repository;

import com.secondhand.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    List<Note> findByIdIn(List<Long> noteIdsInOrder);
}
