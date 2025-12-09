package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.Schedule;
import com.mathfactmissions.teacherscheduler.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Schedule findById(UUID id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("schedule not found"));
    }
}
