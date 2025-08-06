package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // Tìm phòng theo mã phòng
    Optional<Room> findByRoomCode(String roomCode);
    
    // Tìm tất cả phòng đang hoạt động
    List<Room> findByActiveTrue();
    
    // Tìm phòng theo loại
    List<Room> findByRoomTypeAndActiveTrue(String roomType);
    
    // Tìm phòng theo tòa nhà
    List<Room> findByBuildingAndActiveTrue(String building);
    
    // Tìm phòng theo tầng
    List<Room> findByFloorAndActiveTrue(Integer floor);
    
    // Tìm phòng có sức chứa >= số người cần
    @Query("SELECT r FROM Room r WHERE r.capacity >= :minCapacity AND r.active = true")
    List<Room> findRoomsWithMinCapacity(@Param("minCapacity") Integer minCapacity);
    
    // Kiểm tra phòng có trống trong khung giờ không
    @Query("SELECT r FROM Room r WHERE r.active = true AND r.id NOT IN " +
           "(SELECT s.room.id FROM Schedule s WHERE s.dayOfWeek = :dayOfWeek " +
           "AND s.timeSlot.id = :timeSlotId AND s.active = true)")
    List<Room> findAvailableRooms(@Param("dayOfWeek") Integer dayOfWeek, 
                                  @Param("timeSlotId") Long timeSlotId);
    
    // Tìm phòng theo tên (like search)
    @Query("SELECT r FROM Room r WHERE LOWER(r.roomName) LIKE LOWER(CONCAT('%', :name, '%')) AND r.active = true")
    List<Room> findByRoomNameContainingIgnoreCase(@Param("name") String name);
}
