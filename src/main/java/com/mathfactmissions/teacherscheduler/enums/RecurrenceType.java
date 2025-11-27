package com.mathfactmissions.teacherscheduler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum RecurrenceType {
    DAILY("daily", "Daily"),
    WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly"),
    YEARLY("yearly", "Yearly");

    private final String value;

    @Getter
    private final String displayName;

    RecurrenceType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RecurrenceType fromValue(String value) {
        for (RecurrenceType type : RecurrenceType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid recurrence type: " + value);
    }
}
