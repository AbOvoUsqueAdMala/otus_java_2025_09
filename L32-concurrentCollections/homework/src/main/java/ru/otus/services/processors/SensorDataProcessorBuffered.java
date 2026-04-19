package ru.otus.services.processors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ru.otus.api.SensorDataProcessor;
import ru.otus.api.model.SensorData;
import ru.otus.lib.SensorDataBufferedWriter;

@Slf4j
public class SensorDataProcessorBuffered implements SensorDataProcessor {

    private static final Comparator<SensorData> BY_MEASUREMENT_TIME =
            Comparator.comparing(SensorData::getMeasurementTime);

    private final int bufferSize;
    private final SensorDataBufferedWriter writer;
    private final List<SensorData> dataBuffer = new ArrayList<>();

    public SensorDataProcessorBuffered(int bufferSize, SensorDataBufferedWriter writer) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be greater than zero");
        }
        this.bufferSize = bufferSize;
        this.writer = writer;
    }

    @Override
    public synchronized void process(SensorData data) {
        dataBuffer.add(data);
        if (dataBuffer.size() >= bufferSize) {
            flush();
        }
    }

    public synchronized void flush() {
        if (dataBuffer.isEmpty()) {
            return;
        }

        var bufferedData = new ArrayList<>(dataBuffer);
        bufferedData.sort(BY_MEASUREMENT_TIME);
        dataBuffer.clear();

        try {
            writer.writeBufferedData(bufferedData);
        } catch (Exception e) {
            log.error("Error while writing buffered sensor data", e);
            dataBuffer.addAll(bufferedData);
            dataBuffer.sort(BY_MEASUREMENT_TIME);
        }
    }

    @Override
    public void onProcessingEnd() {
        flush();
    }
}
