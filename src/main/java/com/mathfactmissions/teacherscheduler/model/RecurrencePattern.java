package com.mathfactmissions.teacherscheduler.model;

import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import com.mathfactmissions.teacherscheduler.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrencePattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceType type;

    // Weekly fields
    @Column(name="days_of_week")
    private String daysOfWeek;

    // Monthly fields
    @Enumerated(EnumType.STRING)
    @Column(name="month_pattern_type")
    private MonthPatternType monthPatternType;

    @Column(name="days_of_month")
    private String daysOfMonth;

    @Column(name = "nth_weekday_occurrence")
    private  Integer nthWeekdayOccurrence;

    @Column(name="nth_weekday_day")
    private Integer nthWeekdayDay;

    // Yearly Fields
    @Column(name = "yearly_month")
    private Integer yearlyMonth;

    @Column(name = "yearly_day")
    private  Integer yearlyDay;

    @Column(name="time_of_day", nullable = false)
    private LocalTime timeOfDay;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
