package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.RegistrationPeriod;
import java.time.LocalDateTime;
import java.util.List;

public interface IRegistrationPeriodService {
    
    // CRUD cơ bản
    List<RegistrationPeriod> findAll();
    RegistrationPeriod findById(Long id);
    RegistrationPeriod save(RegistrationPeriod registrationPeriod);
    void deleteById(Long id);
    
    // Tìm theo trạng thái
    List<RegistrationPeriod> findByStatus(RegistrationPeriod.RegistrationStatus status);
    
    // Kiểm tra trạng thái đăng ký
    boolean isRegistrationOpen();
    boolean isRegistrationClosed();
    RegistrationPeriod getCurrentActivePeriod();
    
    // Kiểm tra thời gian đăng ký
    boolean canRegisterNow();
    boolean canUnregisterNow();
    
    // Quản lý trạng thái
    RegistrationPeriod openRegistration(Long periodId);
    RegistrationPeriod closeRegistration(Long periodId);
    RegistrationPeriod cancelRegistration(Long periodId);
    
    // Tự động cập nhật trạng thái dựa trên thời gian
    void updatePeriodStatuses();
    
    // Validation
    String validateRegistrationAction(String action);
}
