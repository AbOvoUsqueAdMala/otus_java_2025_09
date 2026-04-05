package ru.otus.dataprocessor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.otus.model.Measurement;

public class ProcessorAggregator implements Processor {

    @Override
    public Map<String, Double> process(List<Measurement> data) {
        return data.stream()
                .collect(Collectors.groupingBy(
                        Measurement::name, LinkedHashMap::new, Collectors.summingDouble(Measurement::value)));
    }
}
