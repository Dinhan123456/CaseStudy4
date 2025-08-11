package com.codegym.module4casestudy.model;

import javax.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", unique = true, nullable = false, length = 20)
    private String roomCode; // VD: LAB 1, 301, 205

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName; // Tên phòng đầy đủ

    @Column(name = "capacity")
    private Integer capacity; // Sức chứa

    @Column(name = "room_type", length = 50)
    private String roomType; // LAB, CLASSROOM, LECTURE_HALL

    @Column(name = "building", length = 50)
    private String building; // Tòa nhà

    @Column(name = "floor")
    private Integer floor; // Tầng

    @Column(name = "equipment", columnDefinition = "TEXT")
    private String equipment; // Thiết bị có sẵn

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Constructors
    public Room() {}

    public Room(String roomCode, String roomName, Integer capacity, String roomType) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.capacity = capacity;
        this.roomType = roomType;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return roomCode + " - " + roomName;
    }
}
