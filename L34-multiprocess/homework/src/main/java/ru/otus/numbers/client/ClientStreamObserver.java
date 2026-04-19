package ru.otus.numbers.client;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.numbers.protobuf.NumberResponse;

public class ClientStreamObserver implements StreamObserver<NumberResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ClientStreamObserver.class);

    private final LastServerValueHolder valueHolder;
    private final CountDownLatch finishLatch;

    public ClientStreamObserver(LastServerValueHolder valueHolder, CountDownLatch finishLatch) {
        this.valueHolder = valueHolder;
        this.finishLatch = finishLatch;
    }

    @Override
    public void onNext(NumberResponse value) {
        valueHolder.update(value.getValue());
        logger.info("new value:{}", value.getValue());
    }

    @Override
    public void onError(Throwable t) {
        if (Status.fromThrowable(t).getCode() == Status.Code.CANCELLED) {
            logger.info("request cancelled");
        } else {
            logger.error("request failed", t);
        }
        finishLatch.countDown();
    }

    @Override
    public void onCompleted() {
        logger.info("request completed");
        finishLatch.countDown();
    }
}
