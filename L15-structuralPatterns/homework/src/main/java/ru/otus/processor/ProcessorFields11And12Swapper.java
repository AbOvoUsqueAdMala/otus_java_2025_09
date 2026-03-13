package ru.otus.processor;

import ru.otus.model.Message;

public class ProcessorFields11And12Swapper implements Processor {

    @Override
    public Message process(Message message) {

        var tempValue = message.getField11();
        return message.toBuilder()
                .field11(message.getField12())
                .field12(tempValue)
                .build();
    }
}
