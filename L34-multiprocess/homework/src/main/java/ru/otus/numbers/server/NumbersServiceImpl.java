package ru.otus.numbers.server;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import ru.otus.numbers.protobuf.NumberResponse;
import ru.otus.numbers.protobuf.NumbersRequest;
import ru.otus.numbers.protobuf.NumbersServiceGrpc;

public class NumbersServiceImpl extends NumbersServiceGrpc.NumbersServiceImplBase {

    private static final long STREAM_DELAY_SECONDS = 2;

    @Override
    public void getNumbers(NumbersRequest request, StreamObserver<NumberResponse> responseObserver) {
        ServerCallStreamObserver<NumberResponse> serverObserver =
                (ServerCallStreamObserver<NumberResponse>) responseObserver;

        try {
            for (int value = request.getFirstValue() + 1; value <= request.getLastValue(); value++) {
                if (serverObserver.isCancelled()) {
                    return;
                }

                TimeUnit.SECONDS.sleep(STREAM_DELAY_SECONDS);
                responseObserver.onNext(
                        NumberResponse.newBuilder().setValue(value).build());
            }
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            responseObserver.onError(e);
        }
    }
}
