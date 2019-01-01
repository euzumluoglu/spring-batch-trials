package hello.db.repository;

import hello.db.entity.NoteEntity;
import hello.db.entity.PersonEntity;
import hello.dto.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity,Integer> {
}
