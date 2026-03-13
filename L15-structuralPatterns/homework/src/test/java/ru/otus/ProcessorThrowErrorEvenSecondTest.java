package ru.otus;

import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.handler.ComplexProcessor;
import ru.otus.model.Message;
import ru.otus.processor.EvenSecondException;
import ru.otus.processor.Processor;
import ru.otus.processor.ProcessorThrowErrorEvenSecond;

public class ProcessorThrowErrorEvenSecondTest {

    @Test
    @DisplayName("Тестируем выбрасывание исключения при четной секунде")
    void notifyTest() {

        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T10:30:32Z"), ZoneId.systemDefault());

        List<Processor> processors = List.of(new ProcessorThrowErrorEvenSecond(fixedClock));

        Consumer<Exception> errorHandler = mock(Consumer.class);

        var complexProcessor = new ComplexProcessor(processors, errorHandler);

        var message = mock(Message.class);
        complexProcessor.handle(message);
        verify(errorHandler).accept(any(EvenSecondException.class));
    }
}
