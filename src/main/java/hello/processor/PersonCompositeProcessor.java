package hello.processor;

import hello.db.entity.PersonEntity;
import hello.dto.Person;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonCompositeProcessor  implements ItemProcessor<Person, PersonEntity> {

    @Autowired
    private ItemProcessor<Person,PersonEntity> entityProcessor;

    @Autowired
    private  ItemProcessor<Person,Person> upperProcessor;
    @Override
    public PersonEntity process(Person item) throws Exception {
        return entityProcessor.process(upperProcessor.process(item));
    }
}
