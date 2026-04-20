package ru.abovousqueadmala.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import ru.abovousqueadmala.domain.MessageDto;
import ru.abovousqueadmala.service.DataStore;

class DataControllerTest {

    @Test
    void shouldRejectWritesToAggregateRoom() {
        var dataStore = org.mockito.Mockito.mock(DataStore.class);
        var controller = new DataController(dataStore, Schedulers.immediate());

        StepVerifier.create(controller.messageFromChat("1408", new MessageDto("forbidden")))
                .expectErrorSatisfies(error -> {
                    var responseError = (org.springframework.web.server.ResponseStatusException) error;
                    assertEquals(403, responseError.getStatusCode().value());
                })
                .verify();

        verifyNoInteractions(dataStore);
    }
}
