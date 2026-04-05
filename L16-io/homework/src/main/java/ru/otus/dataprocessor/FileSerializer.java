package ru.otus.dataprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Map;

public class FileSerializer implements Serializer {

    private final String fileName;

    public FileSerializer(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void serialize(Map<String, Double> data) {
        try {
            new ObjectMapper().writeValue(Path.of(fileName).toFile(), data);
        } catch (Exception ex) {
            throw new FileProcessException(ex);
        }
    }
}
