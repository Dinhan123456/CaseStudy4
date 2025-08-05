package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Room;
import java.util.List;

public interface IRoomService {
    
    // CRUD cơ bản
    List<Room> findAll();
    Room findById(Long id);
    Room save(Room room);
    void deleteById(Long id);
    
    // Tìm phòng theo mã phòng
    Room findByRoomCode(String roomCode);
    
    // Tìm tất cả phòng đang hoạt động
    List<Room> findAllActive();
    
    // Tìm phòng theo loại
    List<Room> findByRoomType(String roomType);
    
    // Tìm phòng theo tòa nhà
    List<Room> findByBuilding(String building);
    
    // Tìm phòng theo tầng
    List<Room> findByFloor(Integer floor);
    
    // Tìm phòng có sức chứa >= số người cần
    List<Room> findRoomsWithMinCapacity(Integer minCapacity);
    
    // Kiểm tra phòng có trống trong khung giờ không
    List<Room> findAvailableRooms(Integer dayOfWeek, Long timeSlotId);
    
    // Tìm phòng theo tên
    List<Room> findByRoomNameContaining(String name);
    
    // Kiểm tra mã phòng có tồn tại không
    boolean existsByRoomCode(String roomCode);
}
