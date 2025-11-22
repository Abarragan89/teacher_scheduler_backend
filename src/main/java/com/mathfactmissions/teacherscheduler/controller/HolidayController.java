package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.model.Holiday;
import com.mathfactmissions.teacherscheduler.service.HolidayService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping("/{year}")
    public List<Holiday> getHolidays(@PathVariable int year) {
        return holidayService.getHolidays(year);
    }

    @GetMapping("/{year}/{month}")
    public List<Holiday> getHolidaysForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        return holidayService.getHolidaysForMonth(year, month);
    }
}
