package ru.otus.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AlternatingNumberPrinter {

    private static final int FIRST_THREAD = 1;
    private static final int SECOND_THREAD = 2;

    private final Object monitor = new Object();
    private final int minValue;
    private final int maxValue;

    private int nextThreadNumber = FIRST_THREAD;
    private boolean cancelled;

    public AlternatingNumberPrinter() {
        this(1, 10);
    }

    public AlternatingNumberPrinter(int minValue, int maxValue) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("minValue must be less than maxValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public List<PrintEvent> runAndCollect(int printsPerThread) throws InterruptedException {
        var events = new ArrayList<PrintEvent>(printsPerThread * 2);
        run(printsPerThread, events::add);
        return List.copyOf(events);
    }

    public void run(int printsPerThread, Consumer<PrintEvent> listener) throws InterruptedException {

        if (printsPerThread < 0) {
            throw new IllegalArgumentException("printsPerThread must not be negative");
        }
        Objects.requireNonNull(listener, "listener must not be null");

        resetState();
        var failure = new AtomicReference<Throwable>();
        var firstThread = createWorker(FIRST_THREAD, printsPerThread, listener, failure);
        var secondThread = createWorker(SECOND_THREAD, printsPerThread, listener, failure);

        firstThread.start();
        secondThread.start();

        firstThread.join();
        secondThread.join();

        rethrowFailure(failure.get());
    }

    public static void main(String[] args) throws InterruptedException {

        var printer = new AlternatingNumberPrinter();
        printer.run(30, event -> System.out.printf("Thread %d: %d%n", event.threadNumber(), event.value()));
    }

    private void resetState() {
        synchronized (monitor) {
            nextThreadNumber = FIRST_THREAD;
            cancelled = false;
        }
    }

    private Thread createWorker(
            int threadNumber, int printsPerThread, Consumer<PrintEvent> listener, AtomicReference<Throwable> failure) {

        return Thread.ofPlatform()
                .name("Thread " + threadNumber)
                .unstarted(() -> runWorker(threadNumber, printsPerThread, listener, failure));
    }

    private void runWorker(
            int threadNumber, int printsPerThread, Consumer<PrintEvent> listener, AtomicReference<Throwable> failure) {

        var generator = new NumberGenerator(minValue, maxValue);
        try {
            for (int index = 0; index < printsPerThread; index++) {
                int value = generator.current();
                synchronized (monitor) {
                    while (!cancelled && nextThreadNumber != threadNumber) {
                        monitor.wait();
                    }
                    if (cancelled) {
                        return;
                    }
                    listener.accept(new PrintEvent(threadNumber, value, index));
                    nextThreadNumber = otherThread(threadNumber);
                    monitor.notifyAll();
                }
                generator.advance();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            fail(failure, ex);
        } catch (Throwable ex) {
            fail(failure, ex);
        }
    }

    private void fail(AtomicReference<Throwable> failure, Throwable ex) {
        synchronized (monitor) {
            failure.compareAndSet(null, ex);
            cancelled = true;
            monitor.notifyAll();
        }
    }

    private static int otherThread(int threadNumber) {
        return threadNumber == FIRST_THREAD ? SECOND_THREAD : FIRST_THREAD;
    }

    private static void rethrowFailure(Throwable failure) {
        if (failure == null) {
            return;
        }
        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (failure instanceof Error error) {
            throw error;
        }
        throw new IllegalStateException("Worker thread failed", failure);
    }

    public record PrintEvent(int threadNumber, int value, int indexInThread) {}

    private static final class NumberGenerator {
        private final int minValue;
        private final int maxValue;

        private int currentValue;
        private int step = 1;

        private NumberGenerator(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.currentValue = minValue;
        }

        private int current() {
            return currentValue;
        }

        private void advance() {
            if (currentValue == maxValue) {
                step = -1;
            } else if (currentValue == minValue) {
                step = 1;
            }
            currentValue += step;
        }
    }
}
