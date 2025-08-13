package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.TimeSlot;
import java.util.List;

public interface ITimeSlotService {
    
    // CRUD cơ bản
    List<TimeSlot> findAll();
    TimeSlot findById(Long id);
    TimeSlot save(TimeSlot timeSlot);
    void deleteById(Long id);
    
    // Tìm tất cả khung giờ đang hoạt động
    List<TimeSlot> findAllActive();
    
    // Tìm khung giờ theo số tiết
    TimeSlot findByPeriodNumber(Integer periodNumber);
    
    // Tìm khung giờ không phải giờ nghỉ
    List<TimeSlot> findNonBreakTimeSlots();
    
    // Tìm khung giờ là giờ nghỉ
    List<TimeSlot> findBreakTimeSlots();
    
    // Tìm khung giờ theo khoảng thời gian
    List<TimeSlot> findByTimeRange(String startTime, String endTime);
    
    // Kiểm tra số tiết có tồn tại không
    boolean existsByPeriodNumber(Integer periodNumber);
}
