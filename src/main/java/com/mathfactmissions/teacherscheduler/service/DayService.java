package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.DayRequest;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.repository.DayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DayService {

    public final DayRepository dayRepository;

    @Autowired
    public DayService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }


    public Day createOrFindDay(UUID userId, LocalDate dayDate) {

        Optional<Day> existing = dayRepository.findByUserIdAndDayDate(userId, dayDate);
        return existing.orElseGet(() -> {
            Day newDay = new Day();
            newDay.setDayDate(dayDate);
            newDay.setUserId(userId);
            return dayRepository.save(newDay);
        });

    }

    public List<Day> findAllDays() {
        return dayRepository.findAll();
    }
}
