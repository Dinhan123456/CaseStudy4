package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.RegistrationPeriod;
import com.codegym.module4casestudy.repository.RegistrationPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistrationPeriodServiceImpl implements IRegistrationPeriodService {
    
    @Autowired
    private RegistrationPeriodRepository registrationPeriodRepository;
    
    @Override
    public List<RegistrationPeriod> findAll() {
        return registrationPeriodRepository.findAll();
    }
    
    @Override
    public RegistrationPeriod findById(Long id) {
        Optional<RegistrationPeriod> period = registrationPeriodRepository.findById(id);
        return period.orElse(null);
    }
    
    @Override
    public RegistrationPeriod save(RegistrationPeriod registrationPeriod) {
        if (registrationPeriod.getCreatedAt() == null) {
            registrationPeriod.setCreatedAt(LocalDateTime.now());
        }
        registrationPeriod.setUpdatedAt(LocalDateTime.now());
        return registrationPeriodRepository.save(registrationPeriod);
    }
    
    @Override
    public void deleteById(Long id) {
        RegistrationPeriod period = findById(id);
        if (period != null) {
            period.setActive(false);
            save(period);
        }
    }
    
    @Override
    public List<RegistrationPeriod> findByStatus(RegistrationPeriod.RegistrationStatus status) {
        return registrationPeriodRepository.findByStatusAndActiveTrue(status);
    }
    
    @Override
    public boolean isRegistrationOpen() {
        updatePeriodStatuses(); // Cập nhật trạng thái trước khi kiểm tra
        List<RegistrationPeriod> openPeriods = findByStatus(RegistrationPeriod.RegistrationStatus.OPEN);
        return !openPeriods.isEmpty();
    }
    
    @Override
    public boolean isRegistrationClosed() {
        return !isRegistrationOpen();
    }
    
    @Override
    public RegistrationPeriod getCurrentActivePeriod() {
        updatePeriodStatuses();
        List<RegistrationPeriod> openPeriods = findByStatus(RegistrationPeriod.RegistrationStatus.OPEN);
        return openPeriods.isEmpty() ? null : openPeriods.get(0);
    }
    
    @Override
    public boolean canRegisterNow() {
        return isRegistrationOpen();
    }
    
    @Override
    public boolean canUnregisterNow() {
        return isRegistrationOpen(); // Cho phép hủy đăng ký khi vẫn trong thời gian đăng ký
    }
    
    @Override
    public RegistrationPeriod openRegistration(Long periodId) {
        RegistrationPeriod period = findById(periodId);
        if (period != null) {
            period.setStatus(RegistrationPeriod.RegistrationStatus.OPEN);
            return save(period);
        }
        return null;
    }
    
    @Override
    public RegistrationPeriod closeRegistration(Long periodId) {
        RegistrationPeriod period = findById(periodId);
        if (period != null) {
            period.setStatus(RegistrationPeriod.RegistrationStatus.CLOSED);
            return save(period);
        }
        return null;
    }
    
    @Override
    public RegistrationPeriod cancelRegistration(Long periodId) {
        RegistrationPeriod period = findById(periodId);
        if (period != null) {
            period.setStatus(RegistrationPeriod.RegistrationStatus.CANCELLED);
            return save(period);
        }
        return null;
    }
    
    @Override
    public void updatePeriodStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<RegistrationPeriod> allPeriods = registrationPeriodRepository.findByActiveTrue();
        
        for (RegistrationPeriod period : allPeriods) {
            RegistrationPeriod.RegistrationStatus currentStatus = period.getStatus();
            RegistrationPeriod.RegistrationStatus newStatus = determineStatusByTime(period, now);
            
            if (currentStatus != newStatus) {
                period.setStatus(newStatus);
                save(period);
            }
        }
    }
    
    private RegistrationPeriod.RegistrationStatus determineStatusByTime(RegistrationPeriod period, LocalDateTime now) {
        if (period.getStatus() == RegistrationPeriod.RegistrationStatus.CANCELLED) {
            return RegistrationPeriod.RegistrationStatus.CANCELLED; // Không thay đổi nếu đã bị hủy
        }
        
        if (now.isBefore(period.getStartTime())) {
            return RegistrationPeriod.RegistrationStatus.SCHEDULED;
        } else if (now.isAfter(period.getEndTime())) {
            return RegistrationPeriod.RegistrationStatus.CLOSED;
        } else {
            return RegistrationPeriod.RegistrationStatus.OPEN;
        }
    }
    
    @Override
    public String validateRegistrationAction(String action) {
        if (!canRegisterNow()) {
            return "Không thể " + action + "! Kỳ đăng ký đã đóng.";
        }
        return null; // Không có lỗi
    }
}
