package ru.aoplogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestLogging implements TestLoggingInterface {

    private static final Logger logger = LoggerFactory.getLogger(TestLogging.class);

    @Log
    @Override
    public void calculation(int param) {
        logger.debug("method params: {}", param);
    }

    @Log
    @Override
    public void calculation(int param1, int param2) {
        logger.debug("method params: {}, {}", param1, param2);
    }

    @Override
    public String toString() {
        return "TestLogging{}";
    }
}
