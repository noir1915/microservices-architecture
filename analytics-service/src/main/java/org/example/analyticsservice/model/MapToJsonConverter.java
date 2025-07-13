package org.example.analyticsservice.model;

import jakarta.persistence.AttributeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter
public class MapToJsonConverter implements AttributeConverter<Map<String, Integer>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new RuntimeException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<Map<String, Integer>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON to map", e);
        }
    }
}
