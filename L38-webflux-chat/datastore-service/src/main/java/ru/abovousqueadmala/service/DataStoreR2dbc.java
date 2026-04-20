package ru.abovousqueadmala.service;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.abovousqueadmala.domain.Message;
import ru.abovousqueadmala.repository.MessageRepository;

@Service
public class DataStoreR2dbc implements DataStore {
    private static final Logger log = LoggerFactory.getLogger(DataStoreR2dbc.class);
    private static final String AGGREGATE_ROOM_ID = "1408";
    private final MessageRepository messageRepository;
    private final Scheduler workerPool;

    public DataStoreR2dbc(Scheduler workerPool, MessageRepository messageRepository) {
        this.workerPool = workerPool;
        this.messageRepository = messageRepository;
    }

    @Override
    public Mono<Message> saveMessage(Message message) {
        log.info("saveMessage:{}", message);
        return messageRepository.save(message);
    }

    @Override
    public Flux<Message> loadMessages(String roomId) {
        log.info("loadMessages roomId:{}", roomId);
        var messages = isAggregateRoom(roomId)
                ? messageRepository.findAllOrderById().filter(message -> !isAggregateRoom(message.roomId()))
                : messageRepository.findByRoomId(roomId);
        return messages.delayElements(Duration.of(3, SECONDS), workerPool);
    }

    private boolean isAggregateRoom(String roomId) {
        return AGGREGATE_ROOM_ID.equals(roomId);
    }
}
