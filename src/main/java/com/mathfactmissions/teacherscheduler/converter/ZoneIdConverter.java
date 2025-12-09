package com.mathfactmissions.teacherscheduler.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.ZoneId;

@Converter(autoApply = true)
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId zoneId) {
        return zoneId != null ? zoneId.getId() : null;
    }

    @Override
    public ZoneId convertToEntityAttribute(String dbData) {
        return dbData != null ? ZoneId.of(dbData) : null;
    }
}
