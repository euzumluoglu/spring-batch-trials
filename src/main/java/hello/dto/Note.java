package hello.dto;

import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "note")
public class Note {

    private String to;

    private String from;

    private String heading;

    private String body;
}
