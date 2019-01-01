package hello.db.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

@Entity(name = "note")
@Data
@NoArgsConstructor
public class NoteEntity {

    @Id
    @GeneratedValue
    private int id;

    @NonNull
    private String _to;

    @NonNull
    private String _from;

    @NonNull
    private String _heading;

    @NonNull
    private String _body;


    @CreationTimestamp
    private LocalDateTime createDateTime;

    @Version
    private LocalDateTime lastUpdatedDate;
}
