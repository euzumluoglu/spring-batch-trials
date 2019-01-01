package hello.processor;

import hello.db.entity.NoteEntity;
import hello.db.entity.PersonEntity;
import hello.dto.Note;
import hello.dto.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class NoteProcessor implements ItemProcessor<Note, NoteEntity> {

    private static final Logger log = LoggerFactory.getLogger(NoteProcessor.class);

    @Override
    public NoteEntity process(final Note note) throws Exception {

        NoteEntity noteEntity = new NoteEntity();
        noteEntity.set_to(note.getTo());
        noteEntity.set_from(note.getFrom());
        noteEntity.set_body(note.getBody());
        noteEntity.set_heading(note.getHeading());
        log.info("Converting (" + note + ") into (" + noteEntity + ")");

        return noteEntity;
    }

}