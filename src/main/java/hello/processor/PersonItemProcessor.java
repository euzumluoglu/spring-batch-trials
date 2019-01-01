package hello.processor;

import hello.db.entity.PersonEntity;
import hello.dto.Person;
import org.hibernate.TypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, PersonEntity> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public PersonEntity process(final Person person) throws Exception {
        final String firstName = person.getFirstName();
        final String lastName = person.getLastName();

        final PersonEntity transformedPerson = new PersonEntity();
        transformedPerson.setFirstName(firstName);
        transformedPerson.setLastName(lastName);
        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}