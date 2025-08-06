package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.TimeSlot;
import com.codegym.module4casestudy.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TimeSlotServiceImpl implements ITimeSlotService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Override
    public List<TimeSlot> findAll() {
        return timeSlotRepository.findAll();
    }

    @Override
    public TimeSlot findById(Long id) {
        Optional<TimeSlot> timeSlot = timeSlotRepository.findById(id);
        return timeSlot.orElse(null);
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
    }

    @Override
    public void deleteById(Long id) {
        // Soft delete - chỉ đánh dấu inactive
        TimeSlot timeSlot = findById(id);
        if (timeSlot != null) {
            timeSlot.setActive(false);
            save(timeSlot);
        }
    }

    @Override
    public List<TimeSlot> findAllActive() {
        return timeSlotRepository.findByActiveTrueOrderByPeriodNumber();
    }

    @Override
    public TimeSlot findByPeriodNumber(Integer periodNumber) {
        Optional<TimeSlot> timeSlot = timeSlotRepository.findByPeriodNumberAndActiveTrue(periodNumber);
        return timeSlot.orElse(null);
    }

    @Override
    public List<TimeSlot> findNonBreakTimeSlots() {
        return timeSlotRepository.findByIsBreakFalseAndActiveTrueOrderByPeriodNumber();
    }

    @Override
    public List<TimeSlot> findBreakTimeSlots() {
        return timeSlotRepository.findByIsBreakTrueAndActiveTrueOrderByPeriodNumber();
    }

    @Override
    public List<TimeSlot> findByTimeRange(String startTime, String endTime) {
        return timeSlotRepository.findByTimeRange(startTime, endTime);
    }

    @Override
    public boolean existsByPeriodNumber(Integer periodNumber) {
        return timeSlotRepository.findByPeriodNumberAndActiveTrue(periodNumber).isPresent();
    }
}
