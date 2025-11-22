package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.Holiday;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class HolidayService {

    public List<Holiday> getHolidays(int year) {
        List<Holiday> holidays = new ArrayList<>();

        // ----- FIXED-DATE HOLIDAYS -----
        holidays.add(new Holiday("New Year's Day", LocalDate.of(year, 1, 1), "🎉"));
        holidays.add(new Holiday("Groundhog Day", LocalDate.of(year, 2, 2), "🐹"));
        holidays.add(new Holiday("Valentine's Day", LocalDate.of(year, 2, 14), "❤️"));
        holidays.add(new Holiday("St. Patrick's Day", LocalDate.of(year, 3, 17), "🍀"));
        holidays.add(new Holiday("April Fool's Day", LocalDate.of(year, 4, 1), "🤡"));
        holidays.add(new Holiday("Earth Day", LocalDate.of(year, 4, 22), "🌎"));
        holidays.add(new Holiday("Juneteenth", LocalDate.of(year, 6, 19), "🖤"));
        holidays.add(new Holiday("Independence Day", LocalDate.of(year, 7, 4), "🎆"));
        holidays.add(new Holiday("Halloween", LocalDate.of(year, 10, 31), "🎃"));
        holidays.add(new Holiday("Veterans Day", LocalDate.of(year, 11, 11), "🇺🇸"));
        holidays.add(new Holiday("Christmas Day", LocalDate.of(year, 12, 25), "🎄"));
        holidays.add(new Holiday("New Year's Eve", LocalDate.of(year, 12, 31), "🎉"));

        // ----- RULE-BASED HOLIDAYS -----

        // MLK Day – 3rd Monday of January
        holidays.add(new Holiday("Martin Luther King Jr. Day",
                nthWeekdayOfMonth(year, Month.JANUARY, DayOfWeek.MONDAY, 3), "🕊️"));

        // Presidents Day – 3rd Monday of February
        holidays.add(new Holiday("Presidents Day",
                nthWeekdayOfMonth(year, Month.FEBRUARY, DayOfWeek.MONDAY, 3), "🇺🇸"));

        // Mother's Day – 2nd Sunday of May
        holidays.add(new Holiday("Mother's Day",
                nthWeekdayOfMonth(year, Month.MAY, DayOfWeek.SUNDAY, 2), "🌸"));

        // Memorial Day – last Monday of May
        holidays.add(new Holiday("Memorial Day",
                lastWeekdayOfMonth(year, Month.MAY, DayOfWeek.MONDAY), "🪖"));

        // Father's Day – 3rd Sunday of June
        holidays.add(new Holiday("Father's Day",
                nthWeekdayOfMonth(year, Month.JUNE, DayOfWeek.SUNDAY, 3), "👔"));

        // Labor Day – 1st Monday of September
        holidays.add(new Holiday("Labor Day",
                nthWeekdayOfMonth(year, Month.SEPTEMBER, DayOfWeek.MONDAY, 1), "🛠️"));

        // Columbus Day – 2nd Monday of October
        holidays.add(new Holiday("Columbus Day",
                nthWeekdayOfMonth(year, Month.OCTOBER, DayOfWeek.MONDAY, 2), "🧭"));

        // Election Day – First Tuesday after Nov 1
        holidays.add(new Holiday("Election Day",
                firstTuesdayAfter(year, Month.NOVEMBER), "🗳️"));

        // Thanksgiving – 4th Thursday of November
        holidays.add(new Holiday("Thanksgiving",
                nthWeekdayOfMonth(year, Month.NOVEMBER, DayOfWeek.THURSDAY, 4), "🦃"));

        // Black Friday – day after Thanksgiving
        holidays.add(new Holiday("Black Friday",
                nthWeekdayOfMonth(year, Month.NOVEMBER, DayOfWeek.THURSDAY, 4).plusDays(1), "🛍️"));


        // ----- SPECIAL OBSERVANCES -----

        // Arbor Day – last Friday of April
        holidays.add(new Holiday("Arbor Day",
                lastWeekdayOfMonth(year, Month.APRIL, DayOfWeek.FRIDAY), "🌳"));

        return holidays;
    }


    // ----- HELPER METHODS -----

    private LocalDate nthWeekdayOfMonth(int year, Month month, DayOfWeek dayOfWeek, int nth) {
        LocalDate date = LocalDate.of(year, month, 1);

        int diff = dayOfWeek.getValue() - date.getDayOfWeek().getValue();
        if (diff < 0) diff += 7;

        return date.plusDays(diff + (long) (nth - 1) * 7);
    }

    private LocalDate lastWeekdayOfMonth(int year, Month month, DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.of(year, month, month.length(Year.isLeap(year)));

        int diff = date.getDayOfWeek().getValue() - dayOfWeek.getValue();
        if (diff < 0) diff += 7;

        return date.minusDays(diff);
    }

    private LocalDate firstTuesdayAfter(int year, Month month) {
        LocalDate date = LocalDate.of(year, month, 1);

        // Find *first Tuesday*
        while (date.getDayOfWeek() != DayOfWeek.TUESDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    public List<Holiday> getHolidaysForMonth(int year, int month) {
        return getHolidays(year).stream()
                .filter(h -> h.getDate().getMonthValue() == month)
                .toList();
    }

//    private LocalDate nthWeekdayOfMonth(int year, Month month, DayOfWeek day, int nth) {
//        LocalDate date = LocalDate.of(year, month, 1);
//
//        // Go to first desired weekday of the month
//        while (date.getDayOfWeek() != day) {
//            date = date.plusDays(1);
//        }
//
//        return date.plusWeeks(nth - 1);
//    }
//
//    private LocalDate lastWeekdayOfMonth(int year, Month month, DayOfWeek day) {
//        LocalDate date = LocalDate.of(year, month, month.length(LocalDate.of(year, month, 1).isLeapYear()));
//
//        while (date.getDayOfWeek() != day) {
//            date = date.minusDays(1);
//        }
//
//        return date;
//    }
}
