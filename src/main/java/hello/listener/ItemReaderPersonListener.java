package hello.listener;

import hello.dto.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ItemReaderPersonListener implements ItemReadListener<Person> {

    @Override
    public void beforeRead() {
    }

    @Override
    public void afterRead(Person item) {
        log.info("Item was read {}", item);
    }

    @Override
    public void onReadError(Exception ex) {

    }
}
