package hello.db.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.LocalDateTime;


@Entity(name = "person")
@Data
@NoArgsConstructor
public class PersonEntity {


    @Id
    @GeneratedValue
    private int id;

    @NonNull
    private String lastName;

    @NonNull
    private String firstName;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @Version
    private LocalDateTime lastUpdatedDate;

}