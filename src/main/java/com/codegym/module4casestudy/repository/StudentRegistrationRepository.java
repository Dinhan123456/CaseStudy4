package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.StudentRegistration;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.model.Schedule;
import com.codegym.module4casestudy.model.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRegistrationRepository extends JpaRepository<StudentRegistration, Long> {
    
    // Tìm đăng ký theo sinh viên
    List<StudentRegistration> findByStudentAndActiveTrue(User student);
    
    // Tìm đăng ký theo sinh viên và kỳ đăng ký
    List<StudentRegistration> findByStudentAndRegistrationPeriodAndActiveTrue(User student, RegistrationPeriod registrationPeriod);
    
    // Tìm đăng ký theo lịch học
    List<StudentRegistration> findByScheduleAndActiveTrue(Schedule schedule);
    
    // Tìm đăng ký theo kỳ đăng ký
    List<StudentRegistration> findByRegistrationPeriodAndActiveTrue(RegistrationPeriod registrationPeriod);
    
    // Kiểm tra sinh viên đã đăng ký lịch này chưa
    Optional<StudentRegistration> findByStudentAndScheduleAndActiveTrue(User student, Schedule schedule);
    
    // Tìm đăng ký theo trạng thái
    List<StudentRegistration> findByStatusAndActiveTrue(StudentRegistration.RegistrationStatus status);
    
    // Đếm số môn sinh viên đã đăng ký trong kỳ
    @Query("SELECT COUNT(sr) FROM StudentRegistration sr WHERE sr.student.id = :studentId " +
           "AND sr.registrationPeriod.id = :periodId AND sr.active = true")
    Long countStudentRegistrationsInPeriod(@Param("studentId") Long studentId, 
                                           @Param("periodId") Long periodId);
    
    // Tính tổng tín chỉ sinh viên đã đăng ký trong kỳ
    @Query("SELECT COALESCE(SUM(s.subject.credits), 0) FROM StudentRegistration sr " +
           "JOIN sr.schedule s WHERE sr.student.id = :studentId " +
           "AND sr.registrationPeriod.id = :periodId AND sr.active = true")
    Integer sumCreditsForStudentInPeriod(@Param("studentId") Long studentId, 
                                        @Param("periodId") Long periodId);
    
    // Kiểm tra trùng lịch cho sinh viên
    @Query("SELECT sr FROM StudentRegistration sr " +
           "JOIN sr.schedule s WHERE sr.student.id = :studentId " +
           "AND s.dayOfWeek = :dayOfWeek AND s.timeSlot.id = :timeSlotId " +
           "AND sr.active = true")
    List<StudentRegistration> findConflictingRegistrations(@Param("studentId") Long studentId,
                                                          @Param("dayOfWeek") Integer dayOfWeek,
                                                          @Param("timeSlotId") Long timeSlotId);
    
    // Lấy danh sách sinh viên đăng ký một lịch học
    @Query("SELECT sr.student FROM StudentRegistration sr WHERE sr.schedule.id = :scheduleId AND sr.active = true")
    List<User> findStudentsBySchedule(@Param("scheduleId") Long scheduleId);
    
    // Đếm số sinh viên đăng ký một lịch học
    @Query("SELECT COUNT(sr) FROM StudentRegistration sr WHERE sr.schedule.id = :scheduleId AND sr.active = true")
    Long countStudentsBySchedule(@Param("scheduleId") Long scheduleId);
    
    // Các method mới cho service
    List<StudentRegistration> findByActiveTrue();
    
    List<StudentRegistration> findByStudentIdAndActiveTrue(Long studentId);
    
    List<StudentRegistration> findByScheduleIdAndActiveTrue(Long scheduleId);
    
    List<StudentRegistration> findBySubjectIdAndActiveTrue(Long subjectId);
    
    // Sử dụng @Query để tìm theo classEntity.id
    @Query("SELECT sr FROM StudentRegistration sr WHERE sr.classEntity.id = :classId AND sr.active = true")
    List<StudentRegistration> findByClassIdAndActiveTrue(@Param("classId") Long classId);
    
    List<StudentRegistration> findByRegistrationPeriodIdAndActiveTrue(Long periodId);
    
    boolean existsByStudentIdAndScheduleIdAndActiveTrue(Long studentId, Long scheduleId);
    
    boolean existsByStudentIdAndSubjectIdAndActiveTrue(Long studentId, Long subjectId);
    
    @Query("SELECT COUNT(sr) FROM StudentRegistration sr WHERE sr.subject.id = :subjectId AND sr.active = true")
    long countBySubjectIdAndActiveTrue(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(sr) FROM StudentRegistration sr WHERE sr.classEntity.id = :classId AND sr.active = true")
    long countByClassIdAndActiveTrue(@Param("classId") Long classId);
    
    long countByStatusAndActiveTrue(StudentRegistration.RegistrationStatus status);
}
