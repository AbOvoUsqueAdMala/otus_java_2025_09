package ru.otus.numbers.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NumbersServer {
    private static final Logger logger = LoggerFactory.getLogger(NumbersServer.class);
    public static final int SERVER_PORT = 8190;

    private NumbersServer() {}

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(SERVER_PORT)
                .addService(new NumbersServiceImpl())
                .build();

        server.start();
        logger.info("numbers Server is starting...");
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
