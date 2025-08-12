package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    
    // Tìm tất cả kỳ đăng ký đang hoạt động
    List<RegistrationPeriod> findByActiveTrueOrderByStartTimeDesc();
    
    // Tìm theo status và active
    List<RegistrationPeriod> findByStatusAndActiveTrue(RegistrationPeriod.RegistrationStatus status);
    
    // Tìm tất cả đang active
    List<RegistrationPeriod> findByActiveTrue();
    
    // Tìm kỳ đăng ký theo trạng thái
    List<RegistrationPeriod> findByStatusAndActiveTrueOrderByStartTimeDesc(RegistrationPeriod.RegistrationStatus status);
    
    // Tìm kỳ đăng ký đang mở
    @Query("SELECT rp FROM RegistrationPeriod rp WHERE rp.status = 'OPEN' " +
           "AND rp.startTime <= :now AND rp.endTime >= :now AND rp.active = true")
    Optional<RegistrationPeriod> findCurrentOpenPeriod(LocalDateTime now);
    
    // Tìm kỳ đăng ký sắp mở
    @Query("SELECT rp FROM RegistrationPeriod rp WHERE rp.status = 'SCHEDULED' " +
           "AND rp.startTime > :now AND rp.active = true ORDER BY rp.startTime ASC")
    List<RegistrationPeriod> findUpcomingPeriods(LocalDateTime now);
    
    // Tìm kỳ đăng ký đã kết thúc
    @Query("SELECT rp FROM RegistrationPeriod rp WHERE " +
           "(rp.status = 'CLOSED' OR rp.endTime < :now) AND rp.active = true ORDER BY rp.endTime DESC")
    List<RegistrationPeriod> findClosedPeriods(LocalDateTime now);
}
