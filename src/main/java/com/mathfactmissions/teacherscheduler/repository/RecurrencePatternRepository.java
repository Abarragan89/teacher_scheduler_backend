package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.enums.RecurrenceType;
import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurrencePatternRepository extends JpaRepository<RecurrencePattern, Long> {

    /**
     * Find all patterns of a specific type
     */
    List<RecurrencePattern> findByType(RecurrenceType type);

    /**
     * Find patterns by time of day (useful for scheduling optimization)
     */
    List<RecurrencePattern> findByTimeOfDay(LocalTime timeOfDay);

    /**
     * Find weekly patterns that include a specific day
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.type = 'WEEKLY' AND rp.daysOfWeek LIKE CONCAT('%', :day, '%')")
    List<RecurrencePattern> findWeeklyPatternsWithDay(@Param("day") String day);

    /**
     * Find monthly patterns by pattern type (BY_DATE or BY_DAY)
     */
    List<RecurrencePattern> findByTypeAndMonthPatternType(RecurrenceType type, MonthPatternType monthPatternType);

    /**
     * Find monthly patterns that include a specific day of month
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.type = 'MONTHLY' AND rp.monthPatternType = 'BY_DATE' AND rp.daysOfMonth LIKE CONCAT('%', :day, '%')")
    List<RecurrencePattern> findMonthlyPatternsByDay(@Param("day") String day);

    /**
     * Find monthly patterns for nth weekday (e.g., 2nd Tuesday)
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.type = 'MONTHLY' AND rp.monthPatternType = 'BY_DAY' AND rp.nthWeekdayOccurrence = :nth AND rp.nthWeekdayDay = :weekday")
    List<RecurrencePattern> findMonthlyNthWeekdayPatterns(@Param("nth") Integer nth, @Param("weekday") Integer weekday);

    /**
     * Find yearly patterns for a specific month
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.type = 'YEARLY' AND rp.yearlyMonth = :month")
    List<RecurrencePattern> findYearlyPatternsByMonth(@Param("month") Integer month);

    /**
     * Find yearly patterns for a specific date (month + day)
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.type = 'YEARLY' AND rp.yearlyMonth = :month AND rp.yearlyDay = :day")
    List<RecurrencePattern> findYearlyPatternsByDate(@Param("month") Integer month, @Param("day") Integer day);

    /**
     * Find patterns created within a date range (for analytics)
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.createdAt BETWEEN :startDate AND :endDate")
    List<RecurrencePattern> findCreatedBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Find patterns created by time range (for performance analysis)
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.timeOfDay BETWEEN :startTime AND :endTime")
    List<RecurrencePattern> findByTimeOfDayBetween(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    /**
     * Find all patterns that should generate todos today (useful for batch processing)
     */
    @Query(value = """
        SELECT rp FROM RecurrencePattern rp 
        WHERE (rp.type = 'DAILY') 
        OR (rp.type = 'WEEKLY' AND rp.daysOfWeek LIKE CONCAT('%', :todayDayOfWeek, '%'))
        OR (rp.type = 'MONTHLY' AND rp.monthPatternType = 'BY_DATE' AND rp.daysOfMonth LIKE CONCAT('%', :todayDayOfMonth, '%'))
        OR (rp.type = 'YEARLY' AND rp.yearlyMonth = :currentMonth AND rp.yearlyDay = :todayDayOfMonth)
        """)
    List<RecurrencePattern> findPatternsDueToday(
            @Param("todayDayOfWeek") String todayDayOfWeek,
            @Param("todayDayOfMonth") String todayDayOfMonth,
            @Param("currentMonth") Integer currentMonth
    );

    /**
     * Count patterns by type (for dashboard statistics)
     */
    @Query("SELECT rp.type, COUNT(rp) FROM RecurrencePattern rp GROUP BY rp.type")
    List<Object[]> countPatternsByType();

    /**
     * Find most common time slots (for optimization)
     */
    @Query("SELECT rp.timeOfDay, COUNT(rp) FROM RecurrencePattern rp GROUP BY rp.timeOfDay ORDER BY COUNT(rp) DESC")
    List<Object[]> findMostCommonTimeslots();

    /**
     * Find orphaned patterns (not used by any todos)
     */
    @Query("SELECT rp FROM RecurrencePattern rp WHERE NOT EXISTS (SELECT t FROM Todo t WHERE t.recurrencePattern = rp)")
    List<RecurrencePattern> findOrphanedPatterns();

    /**
     * Check if pattern exists with exact same configuration (prevent duplicates)
     */
    @Query("""
        SELECT rp FROM RecurrencePattern rp 
        WHERE rp.type = :type 
        AND rp.timeOfDay = :timeOfDay
        AND (:daysOfWeek IS NULL OR rp.daysOfWeek = :daysOfWeek)
        AND (:monthPatternType IS NULL OR rp.monthPatternType = :monthPatternType)
        AND (:daysOfMonth IS NULL OR rp.daysOfMonth = :daysOfMonth)
        AND (:nthWeekdayOccurrence IS NULL OR rp.nthWeekdayOccurrence = :nthWeekdayOccurrence)
        AND (:nthWeekdayDay IS NULL OR rp.nthWeekdayDay = :nthWeekdayDay)
        AND (:yearlyMonth IS NULL OR rp.yearlyMonth = :yearlyMonth)
        AND (:yearlyDay IS NULL OR rp.yearlyDay = :yearlyDay)
        """)
    Optional<RecurrencePattern> findExistingPattern(
            @Param("type") RecurrenceType type,
            @Param("timeOfDay") LocalTime timeOfDay,
            @Param("daysOfWeek") String daysOfWeek,
            @Param("monthPatternType") MonthPatternType monthPatternType,
            @Param("daysOfMonth") String daysOfMonth,
            @Param("nthWeekdayOccurrence") Integer nthWeekdayOccurrence,
            @Param("nthWeekdayDay") Integer nthWeekdayDay,
            @Param("yearlyMonth") Integer yearlyMonth,
            @Param("yearlyDay") Integer yearlyDay
    );
}