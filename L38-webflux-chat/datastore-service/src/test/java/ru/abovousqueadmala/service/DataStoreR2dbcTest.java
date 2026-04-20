package ru.abovousqueadmala.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import ru.abovousqueadmala.domain.Message;
import ru.abovousqueadmala.repository.MessageRepository;

class DataStoreR2dbcTest {

    @Test
    void shouldLoadAllMessagesForAggregateRoom() {
        var repository = org.mockito.Mockito.mock(MessageRepository.class);
        when(repository.findAllOrderById())
                .thenReturn(Flux.just(
                        new Message(1L, "1", "first"),
                        new Message(2L, "1408", "should be filtered"),
                        new Message(3L, "2", "second")));

        var scheduler = VirtualTimeScheduler.create();
        var dataStore = new DataStoreR2dbc(scheduler, repository);

        StepVerifier.withVirtualTime(() -> dataStore.loadMessages("1408"), () -> scheduler, Long.MAX_VALUE)
                .thenAwait(Duration.ofSeconds(9))
                .expectNext(new Message(1L, "1", "first"))
                .expectNext(new Message(3L, "2", "second"))
                .verifyComplete();

        verify(repository).findAllOrderById();
        verify(repository, never()).findByRoomId("1408");
    }

    @Test
    void shouldLoadMessagesForRegularRoom() {
        var repository = org.mockito.Mockito.mock(MessageRepository.class);
        when(repository.findByRoomId("7")).thenReturn(Flux.just(new Message(10L, "7", "room message")));

        var scheduler = VirtualTimeScheduler.create();
        var dataStore = new DataStoreR2dbc(scheduler, repository);

        StepVerifier.withVirtualTime(() -> dataStore.loadMessages("7"), () -> scheduler, Long.MAX_VALUE)
                .thenAwait(Duration.ofSeconds(3))
                .expectNext(new Message(10L, "7", "room message"))
                .verifyComplete();

        verify(repository).findByRoomId("7");
        verify(repository, never()).findAllOrderById();
    }
}
