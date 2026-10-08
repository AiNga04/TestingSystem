package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;

@Converter
public class PositionNameConverter
        implements AttributeConverter<PositionName, String> {

    @Override
    public String convertToDatabaseColumn(PositionName value) {
        if (value == null) return null;

        return switch (value) {
            case DEV -> "Dev";
            case TEST -> "Test";
            case SCRUM_MASTER -> "Scrum Master";
            case PM -> "PM";
        };
    }

    @Override
    public PositionName convertToEntityAttribute(String value) {
        if (value == null) return null;

        return switch (value) {
            case "Dev" -> PositionName.DEV;
            case "Test" -> PositionName.TEST;
            case "Scrum Master" -> PositionName.SCRUM_MASTER;
            case "PM" -> PositionName.PM;
            default -> throw new IllegalArgumentException(
                    "Unknown position: " + value
            );
        };
    }
}
