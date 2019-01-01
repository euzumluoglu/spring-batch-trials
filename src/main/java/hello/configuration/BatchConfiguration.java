package hello.configuration;

import javax.persistence.EntityManagerFactory;

import hello.db.entity.NoteEntity;
import hello.db.entity.PersonEntity;
import hello.db.repository.PersonRepository;
import hello.dto.Note;
import hello.dto.Person;
import hello.listener.DefaultChunkListener;
import hello.listener.DefaultStepExecutionListener;
import hello.listener.ItemReaderPersonListener;
import hello.listener.JobCompletionNotificationListener;
import hello.policy.FileVerificationSkipper;
import hello.processor.NoteProcessor;
import hello.processor.PersonItemProcessor;
import hello.processor.PersonItemUpperProcessor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.CompositeItemProcessListener;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemReaderException;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.amqp.AmqpItemReader;
import org.springframework.batch.item.amqp.AmqpItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private EntityManagerFactory emf;

    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader<Person> fileReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    public ItemReader<Note> xmlNoteReader(){
        StaxEventItemReader<Note> reader = new StaxEventItemReader<Note>();
        reader.setResource( new ClassPathResource("sample-data.xml") );
        reader.setFragmentRootElementName("note");

        Jaxb2Marshaller noteMarshaller = new Jaxb2Marshaller();
        noteMarshaller.setClassesToBeBound(Note.class);
        reader.setUnmarshaller(noteMarshaller);
        return reader;
    }

    @Bean
    public ItemProcessor entityProcessor() {
        return new PersonItemProcessor();
    }

    @Bean
    public ItemProcessor upperProcessor() {
        return new PersonItemUpperProcessor();
    }

    @Bean
    public ItemProcessor noteProcessor() {
        return new NoteProcessor();
    }

    @Bean
    public CompositeItemProcessor<Person, PersonEntity> compositePersonProcessor(ItemProcessor entityProcessor,
                                                                                 ItemProcessor upperProcessor) {
        CompositeItemProcessor<Person, PersonEntity> compositeItemProcessor
                = new CompositeItemProcessor<Person, PersonEntity>();
        List itemProcessors = new ArrayList();
        itemProcessors.add(upperProcessor);
        itemProcessors.add(entityProcessor);
        compositeItemProcessor.setDelegates(itemProcessors);
        return compositeItemProcessor;
    }


    @Bean
    public AmqpItemReader<Person> personAmqpItemReader(AmqpTemplate amqpTemplate) {
        AmqpItemReader personAmqpItemReader = new AmqpItemReader(amqpTemplate);
        return personAmqpItemReader;
    }


    @Bean
    public AmqpItemWriter<Person> personAmqpItemWriter(AmqpTemplate amqpTemplate) {
        AmqpItemWriter personAmqpItemWriter = new AmqpItemWriter(amqpTemplate);
        return personAmqpItemWriter;
    }


    @Bean
    public JpaItemWriter<PersonEntity> jpaWriter() {
        JpaItemWriter writer = new JpaItemWriter();
        writer.setEntityManagerFactory(emf);
        return writer;
    }

    @Bean
    public JpaItemWriter<NoteEntity> jpaNoteWriter() {
        JpaItemWriter writer = new JpaItemWriter();
        writer.setEntityManagerFactory(emf);
        return writer;
    }

    @Bean
    public SkipPolicy fileVerificationSkipper() {
        return new FileVerificationSkipper();
    }

    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1, Step step2, Step step3, Step step4) {
        return jobBuilderFactory.get("importUserAndProcessJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
//                .flow(step1)
//                .next(step2)
                .flow(step4)
                .end()
                .build();
    }

    @Bean
    public Job importNoteJob(Step stepNote1) {
        return jobBuilderFactory.get("importNoteAndProcessJob")
                .incrementer(new RunIdIncrementer())
                .flow(stepNote1)
                .end()
                .build();
    }

    @Bean
    public Step step1(final AmqpTemplate amqpTemplate,
                      final DefaultStepExecutionListener defaultStepExecutionListener,
                      final DefaultChunkListener defaultChunkListener,
                      final ItemReaderPersonListener itemReaderPersonListener) {
        Step step = stepBuilderFactory.get("step1-import-user")
                .<Person, PersonEntity>chunk(3)
                .reader(fileReader())
                .processor(upperProcessor())
                .writer(personAmqpItemWriter(amqpTemplate))
                .listener(itemReaderPersonListener)
//                 .listener(defaultStepExecutionListener)
//                 .faultTolerant()
//                 .skipPolicy(fileVerificationSkipper())
//                 .skipLimit(5)
//                 .skip(Exception.class)
//                 .retryLimit(50).retry(TypeMismatchException.class)
//                 .noSkip(FlatFileParseException.class)
                .build();
//        StepExecutionListener[] stepExecutionListeners = new StepExecutionListener[1];
//        stepExecutionListeners[0] = defaultStepExecutionListener;
        ((TaskletStep) step).registerStepExecutionListener(defaultStepExecutionListener);
        ((TaskletStep) step).registerChunkListener(defaultChunkListener);

//         ((TaskletStep) step).setStartLimit(1);
//         ((TaskletStep) s).setAllowStartIfComplete(true);
        return step;
    }

    @Bean
    public Step step2(final AmqpTemplate amqpTemplate, final DefaultStepExecutionListener defaultStepExecutionListener) {
        Step step = stepBuilderFactory.get("step2-convert-user")
                .<Person, PersonEntity>chunk(10)
                .reader(personAmqpItemReader(amqpTemplate))
                .processor(entityProcessor())
                .writer(jpaWriter())
//                .readerIsTransactionalQueue()
                .build();

//        StepExecutionListener[] stepExecutionListeners = new StepExecutionListener[1];
//        stepExecutionListeners[0] = defaultStepExecutionListener;
        ((TaskletStep) step).registerStepExecutionListener(defaultStepExecutionListener);
        return step;
    }
    // end::jobstep[]

    @Bean
    public Step step3(final ItemProcessor personCompositeProcessor, final DefaultStepExecutionListener defaultStepExecutionListener) {
        Step step = stepBuilderFactory.get("step3-composite-user")
                .<Person, PersonEntity>chunk(10)
                .reader(fileReader())
                .processor(personCompositeProcessor)
                .writer(jpaWriter())
//                .readerIsTransactionalQueue()
                .build();
//        StepExecutionListener[] stepExecutionListeners = new StepExecutionListener[1];
//        stepExecutionListeners[0] = defaultStepExecutionListener;
        ((TaskletStep) step).registerStepExecutionListener(defaultStepExecutionListener);
        return step;
    }

    @Bean
    public Step step4(final CompositeItemProcessor compositePersonProcessor, final DefaultStepExecutionListener defaultStepExecutionListener) {
        Step step = stepBuilderFactory.get("step4-composite-user")
                .<Person, PersonEntity>chunk(10)
                .reader(fileReader())
                .processor(compositePersonProcessor)
                .writer(jpaWriter())
//                .readerIsTransactionalQueue()
                .build();
//        StepExecutionListener[] stepExecutionListeners = new StepExecutionListener[1];
//        stepExecutionListeners[0] = defaultStepExecutionListener;
        ((TaskletStep) step).registerStepExecutionListener(defaultStepExecutionListener);
        return step;
    }

    @Bean
    public Step stepNote1(final ItemReader xmlNoteReader, final ItemProcessor noteProcessor, final ItemWriter jpaNoteWriter) {
        Step step = stepBuilderFactory.get("step1-note")
                .<Person, PersonEntity>chunk(10)
                .reader(xmlNoteReader)
                .processor(noteProcessor)
                .writer(jpaNoteWriter)
                .build();
        return step;
    }
}
