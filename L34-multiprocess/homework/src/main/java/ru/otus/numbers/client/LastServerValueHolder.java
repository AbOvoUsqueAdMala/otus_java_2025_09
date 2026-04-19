package ru.otus.numbers.client;

import java.util.concurrent.atomic.AtomicInteger;

public class LastServerValueHolder {

    static final int NO_VALUE = Integer.MIN_VALUE;

    private final AtomicInteger latestValue = new AtomicInteger(NO_VALUE);

    public void update(int value) {
        latestValue.set(value);
    }

    public int consume() {
        return latestValue.getAndSet(NO_VALUE);
    }
}
