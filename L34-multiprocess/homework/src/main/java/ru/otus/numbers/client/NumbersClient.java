package ru.otus.numbers.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.numbers.protobuf.NumbersRequest;
import ru.otus.numbers.protobuf.NumbersServiceGrpc;

public final class NumbersClient {

    private static final Logger logger = LoggerFactory.getLogger(NumbersClient.class);

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8190;
    private static final int FIRST_VALUE = 0;
    private static final int LAST_VALUE = 30;
    private static final int LOOP_BOUND = 50;

    private NumbersClient() {}

    public static void main(String[] args) throws InterruptedException {
        logger.info("numbers Client is starting...");

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext()
                .build();

        LastServerValueHolder valueHolder = new LastServerValueHolder();
        CountDownLatch finishLatch = new CountDownLatch(1);

        NumbersServiceGrpc.newStub(channel)
                .getNumbers(
                        NumbersRequest.newBuilder()
                                .setFirstValue(FIRST_VALUE)
                                .setLastValue(LAST_VALUE)
                                .build(),
                        new ClientStreamObserver(valueHolder, finishLatch));

        int currentValue = 0;
        try {
            for (int index = 0; index <= LOOP_BOUND; index++) {
                int serverValue = valueHolder.consume();
                if (serverValue != LastServerValueHolder.NO_VALUE) {
                    currentValue += serverValue;
                }
                currentValue += 1;
                logger.info("currentValue:{}", currentValue);

                if (index < LOOP_BOUND) {
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        } finally {
            channel.shutdownNow();
            if (!finishLatch.await(5, TimeUnit.SECONDS)) {
                logger.warn("stream did not finish before timeout");
            }
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("channel did not terminate before timeout");
            }
        }
    }
}
