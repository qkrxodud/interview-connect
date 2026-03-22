package com.ic.domain.review;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (Objects.isNull(attribute) || attribute.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("문자열 목록을 JSON으로 변환하는 중 오류가 발생했습니다", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (Objects.isNull(dbData) || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON을 문자열 목록으로 변환하는 중 오류가 발생했습니다", e);
        }
    }
}