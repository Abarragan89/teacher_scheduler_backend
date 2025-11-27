package com.mathfactmissions.teacherscheduler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum MonthPatternType {
    BY_DATE("by_date", "By Date"),
    BY_DAY("by_day", "By Day of Week");

    private final String value;

    @Getter
    private final String displayName;

    MonthPatternType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static MonthPatternType fromValue(String value) {
        for (MonthPatternType type : MonthPatternType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid month pattern type: " + value);
    }
}
