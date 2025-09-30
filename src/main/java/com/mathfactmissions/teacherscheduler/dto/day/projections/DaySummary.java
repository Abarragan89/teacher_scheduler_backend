package com.mathfactmissions.teacherscheduler.dto.day.projections;

import java.time.LocalDate;
import java.util.UUID;

public interface DaySummary {
    UUID getId();
    LocalDate getDayDate();
}
