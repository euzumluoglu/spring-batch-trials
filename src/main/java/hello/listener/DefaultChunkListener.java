package hello.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        log.info("Before Chunk calling {}",context.getStepContext().getStepName());
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("After Chunk calling {}",context.getStepContext().getStepName());
    }

    @Override
    public void afterChunkError(ChunkContext context) {

    }
}
