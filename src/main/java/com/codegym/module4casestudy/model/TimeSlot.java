package com.codegym.module4casestudy.model;

import javax.persistence.*;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber; // Tiết thứ: 1, 2, 3, 4, 5, 6, 7, 8

    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime; // VD: "07:00"

    @Column(name = "end_time", nullable = false, length = 10)
    private String endTime; // VD: "07:45"

    @Column(name = "period_name", length = 50)
    private String periodName; // VD: "Tiết 1", "Nghỉ", "Nghỉ trưa"

    @Column(name = "is_break", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBreak = false; // Có phải giờ nghỉ không

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Constructors
    public TimeSlot() {}

    public TimeSlot(Integer periodNumber, String startTime, String endTime, String periodName) {
        this.periodNumber = periodNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.periodName = periodName;
        this.isBreak = false;
        this.active = true;
    }

    public TimeSlot(Integer periodNumber, String startTime, String endTime, String periodName, Boolean isBreak) {
        this.periodNumber = periodNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.periodName = periodName;
        this.isBreak = isBreak;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(Integer periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public Boolean getIsBreak() {
        return isBreak;
    }

    public void setIsBreak(Boolean isBreak) {
        this.isBreak = isBreak;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getTimeRange() {
        return startTime + " - " + endTime;
    }

    @Override
    public String toString() {
        return periodName + " (" + startTime + " - " + endTime + ")";
    }
}
