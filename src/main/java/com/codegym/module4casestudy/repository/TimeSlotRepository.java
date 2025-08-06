package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    // Tìm tất cả khung giờ đang hoạt động
    List<TimeSlot> findByActiveTrueOrderByPeriodNumber();
    
    // Tìm khung giờ theo số tiết
    Optional<TimeSlot> findByPeriodNumberAndActiveTrue(Integer periodNumber);
    
    // Tìm khung giờ không phải giờ nghỉ
    List<TimeSlot> findByIsBreakFalseAndActiveTrueOrderByPeriodNumber();
    
    // Tìm khung giờ là giờ nghỉ
    List<TimeSlot> findByIsBreakTrueAndActiveTrueOrderByPeriodNumber();
    
    // Tìm khung giờ theo khoảng thời gian
    @Query("SELECT t FROM TimeSlot t WHERE t.startTime >= :startTime AND t.endTime <= :endTime AND t.active = true ORDER BY t.periodNumber")
    List<TimeSlot> findByTimeRange(String startTime, String endTime);
}
