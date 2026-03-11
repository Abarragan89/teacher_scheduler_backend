package com.mathfactmissions.teacherscheduler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum RecurrenceType {
    DAILY("DAILY", "Daily"),
    WEEKLY("WEEKLY", "Weekly"),
    MONTHLY("MONTHLY", "Monthly"),
    YEARLY("YEARLY", "Yearly");
    
    private final String value;
    
    @Getter
    private final String displayName;
    
    RecurrenceType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public static RecurrenceType fromValue(String value) {
        for (RecurrenceType type : RecurrenceType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid recurrence type: " + value);
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
}
