package hello.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Data
//@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class Person {

    private String lastName;
    private String firstName;

}