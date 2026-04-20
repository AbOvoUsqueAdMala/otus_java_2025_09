package ru.abovousqueadmala.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.abovousqueadmala.domain.Message;

class MessageControllerTest {

    @Test
    void shouldMirrorMessageToAggregateRoom() {
        var template = org.mockito.Mockito.mock(SimpMessagingTemplate.class);
        var webClient = WebClient.builder()
                .baseUrl("http://localhost")
                .exchangeFunction(request -> {
                    assertEquals("/msg/1", request.url().getPath());
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("1")
                            .build());
                })
                .build();
        var controller = new MessageController(webClient, template);

        controller.getMessage("1", new Message("<b>hello</b>"));

        var escapedMessage = new Message("&lt;b&gt;hello&lt;/b&gt;");
        verify(template).convertAndSend("/topic/response.1", escapedMessage);
        verify(template).convertAndSend("/topic/response.1408", escapedMessage);
    }

    @Test
    void shouldIgnoreMessagesToAggregateRoom() {
        var template = org.mockito.Mockito.mock(SimpMessagingTemplate.class);
        var clientCalled = new AtomicBoolean(false);
        var webClient = WebClient.builder()
                .baseUrl("http://localhost")
                .exchangeFunction(request -> {
                    clientCalled.set(true);
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("1")
                            .build());
                })
                .build();
        var controller = new MessageController(webClient, template);

        controller.getMessage("1408", new Message("forbidden"));

        assertFalse(clientCalled.get());
        verifyNoInteractions(template);
    }
}
