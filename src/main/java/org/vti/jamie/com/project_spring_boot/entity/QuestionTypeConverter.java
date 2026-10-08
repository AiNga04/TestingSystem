package org.vti.jamie.com.project_spring_boot.entity;


import jakarta.persistence.*;
import org.vti.jamie.com.project_spring_boot.enums.QuestionTypeName;

@Converter
public class QuestionTypeConverter
        implements AttributeConverter<QuestionTypeName, String> {

    @Override
    public String convertToDatabaseColumn(QuestionTypeName value) {
        if (value == null) return null;

        return switch (value) {
            case ESSAY -> "Essay";
            case MULTIPLE_CHOICE -> "Multiple-Choice";
        };
    }

    @Override
    public QuestionTypeName convertToEntityAttribute(String value) {
        if (value == null) return null;

        return switch (value) {
            case "Essay" -> QuestionTypeName.ESSAY;
            case "Multiple-Choice" -> QuestionTypeName.MULTIPLE_CHOICE;
            default -> throw new IllegalArgumentException(
                    "Unknown question type: " + value
            );
        };
    }
}
