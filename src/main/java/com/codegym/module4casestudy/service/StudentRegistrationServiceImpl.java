package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.StudentRegistration;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.model.Schedule;
import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.RegistrationPeriod;
import com.codegym.module4casestudy.repository.StudentRegistrationRepository;
import com.codegym.module4casestudy.repository.UserRepository;
import com.codegym.module4casestudy.repository.SubjectRepository;
import com.codegym.module4casestudy.repository.ScheduleRepository;
import com.codegym.module4casestudy.repository.ClassRepository;
import com.codegym.module4casestudy.repository.RegistrationPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentRegistrationServiceImpl implements IStudentRegistrationService {

    @Autowired
    private StudentRegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private RegistrationPeriodRepository periodRepository;

    @Override
    public List<StudentRegistration> findAll() {
        return registrationRepository.findAll();
    }

    @Override
    public List<StudentRegistration> findAllActive() {
        return registrationRepository.findByActiveTrue();
    }

    @Override
    public Optional<StudentRegistration> findById(Long id) {
        return registrationRepository.findById(id);
    }

    @Override
    public StudentRegistration save(StudentRegistration registration) {
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setActive(true);
        return registrationRepository.save(registration);
    }

    @Override
    public StudentRegistration update(Long id, StudentRegistration registration) {
        Optional<StudentRegistration> existing = registrationRepository.findById(id);
        if (existing.isPresent()) {
            StudentRegistration existingRegistration = existing.get();
            existingRegistration.setStatus(registration.getStatus());
            existingRegistration.setNotes(registration.getNotes());
            return registrationRepository.save(existingRegistration);
        }
        throw new RuntimeException("Không tìm thấy đăng ký với ID: " + id);
    }

    @Override
    public void deleteById(Long id) {
        Optional<StudentRegistration> registration = registrationRepository.findById(id);
        if (registration.isPresent()) {
            registration.get().setActive(false);
            registrationRepository.save(registration.get());
        }
    }

    @Override
    public List<StudentRegistration> findByStudentId(Long studentId) {
        return registrationRepository.findByStudentIdAndActiveTrue(studentId);
    }

    @Override
    public List<StudentRegistration> findByScheduleId(Long scheduleId) {
        return registrationRepository.findByScheduleIdAndActiveTrue(scheduleId);
    }

    @Override
    public List<StudentRegistration> findBySubjectId(Long subjectId) {
        return registrationRepository.findBySubjectIdAndActiveTrue(subjectId);
    }

    @Override
    public List<StudentRegistration> findByClassId(Long classId) {
        return registrationRepository.findByClassIdAndActiveTrue(classId);
    }

    @Override
    public List<StudentRegistration> findByRegistrationPeriodId(Long periodId) {
        return registrationRepository.findByRegistrationPeriodIdAndActiveTrue(periodId);
    }

    @Override
    public List<StudentRegistration> findByStatus(StudentRegistration.RegistrationStatus status) {
        return registrationRepository.findByStatusAndActiveTrue(status);
    }

    @Override
    public boolean existsByStudentIdAndScheduleId(Long studentId, Long scheduleId) {
        return registrationRepository.existsByStudentIdAndScheduleIdAndActiveTrue(studentId, scheduleId);
    }

    @Override
    public boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId) {
        return registrationRepository.existsByStudentIdAndSubjectIdAndActiveTrue(studentId, subjectId);
    }

    @Override
    public StudentRegistration registerStudentToSubject(Long studentId, Long subjectId, Long classId, String notes) {
        // Kiểm tra sinh viên đã đăng ký môn học này chưa
        if (existsByStudentIdAndSubjectId(studentId, subjectId)) {
            throw new RuntimeException("Sinh viên đã đăng ký môn học này!");
        }

        // Lấy thông tin sinh viên, môn học, lớp học
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học!"));
        
        Class classEntity = null;
        if (classId != null) {
            classEntity = classRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));
        }

        // Tìm kỳ đăng ký hiện tại (tạm thời tạo mới nếu chưa có)
        RegistrationPeriod currentPeriod = periodRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    RegistrationPeriod newPeriod = new RegistrationPeriod();
                    newPeriod.setPeriodName("Kỳ đăng ký 2024-2025");
                    newPeriod.setStartTime(java.time.LocalDateTime.now());
                    newPeriod.setEndTime(java.time.LocalDateTime.now().plusMonths(6));
                    newPeriod.setStatus(RegistrationPeriod.RegistrationStatus.OPEN);
                    newPeriod.setActive(true);
                    return periodRepository.save(newPeriod);
                });

        // Tạo đăng ký mới
        StudentRegistration registration = new StudentRegistration();
        registration.setStudent(student);
        registration.setSubject(subject);
        registration.setClassEntity(classEntity);
        registration.setRegistrationPeriod(currentPeriod);
        registration.setStatus(StudentRegistration.RegistrationStatus.REGISTERED);
        registration.setNotes(notes);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setActive(true);

        return registrationRepository.save(registration);
    }

    @Override
    public StudentRegistration registerStudentToSchedule(Long studentId, Long scheduleId, String notes) {
        // Kiểm tra sinh viên đã đăng ký lịch học này chưa
        if (existsByStudentIdAndScheduleId(studentId, scheduleId)) {
            throw new RuntimeException("Sinh viên đã đăng ký lịch học này!");
        }

        // Lấy thông tin sinh viên và lịch học
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));
        
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch học!"));

        // Tìm kỳ đăng ký hiện tại (tạm thời tạo mới nếu chưa có)
        RegistrationPeriod currentPeriod = periodRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    RegistrationPeriod newPeriod = new RegistrationPeriod();
                    newPeriod.setPeriodName("Kỳ đăng ký 2024-2025");
                    newPeriod.setStartTime(java.time.LocalDateTime.now());
                    newPeriod.setEndTime(java.time.LocalDateTime.now().plusMonths(6));
                    newPeriod.setStatus(RegistrationPeriod.RegistrationStatus.OPEN);
                    newPeriod.setActive(true);
                    return periodRepository.save(newPeriod);
                });

        // Tạo đăng ký mới
        StudentRegistration registration = new StudentRegistration();
        registration.setStudent(student);
        registration.setSchedule(schedule);
        registration.setRegistrationPeriod(currentPeriod);
        registration.setStatus(StudentRegistration.RegistrationStatus.REGISTERED);
        registration.setNotes(notes);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setActive(true);

        return registrationRepository.save(registration);
    }

    @Override
    public StudentRegistration cancelRegistration(Long registrationId, String reason) {
        Optional<StudentRegistration> registration = registrationRepository.findById(registrationId);
        if (registration.isPresent()) {
            StudentRegistration reg = registration.get();
            reg.setStatus(StudentRegistration.RegistrationStatus.CANCELLED);
            reg.setNotes(reason);
            return registrationRepository.save(reg);
        }
        throw new RuntimeException("Không tìm thấy đăng ký!");
    }

    @Override
    public StudentRegistration confirmRegistration(Long registrationId) {
        Optional<StudentRegistration> registration = registrationRepository.findById(registrationId);
        if (registration.isPresent()) {
            StudentRegistration reg = registration.get();
            reg.setStatus(StudentRegistration.RegistrationStatus.CONFIRMED);
            return registrationRepository.save(reg);
        }
        throw new RuntimeException("Không tìm thấy đăng ký!");
    }

    @Override
    public long countBySubjectId(Long subjectId) {
        return registrationRepository.countBySubjectIdAndActiveTrue(subjectId);
    }

    @Override
    public long countByClassId(Long classId) {
        return registrationRepository.countByClassIdAndActiveTrue(classId);
    }

    @Override
    public long countByStatus(StudentRegistration.RegistrationStatus status) {
        return registrationRepository.countByStatusAndActiveTrue(status);
    }
}
