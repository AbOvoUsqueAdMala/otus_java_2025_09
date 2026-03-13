package ru.otus.processor;

import java.time.Clock;
import java.time.LocalTime;
import ru.otus.model.Message;

public class ProcessorThrowErrorEvenSecond implements Processor {

    private final Clock clock;

    public ProcessorThrowErrorEvenSecond(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Message process(Message message) {

        int currentSecond = LocalTime.now(clock).getSecond();
        if (currentSecond % 2 == 0) {
            throw new EvenSecondException("Метод выполнился в четную секунду");
        }

        return message;
    }
}
