package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Room;
import com.codegym.module4casestudy.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomServiceImpl implements IRoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public Room findById(Long id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    @Override
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public void deleteById(Long id) {
        // Soft delete - chỉ đánh dấu inactive
        Room room = findById(id);
        if (room != null) {
            room.setActive(false);
            save(room);
        }
    }

    @Override
    public Room findByRoomCode(String roomCode) {
        Optional<Room> room = roomRepository.findByRoomCode(roomCode);
        return room.orElse(null);
    }

    @Override
    public List<Room> findAllActive() {
        return roomRepository.findByActiveTrue();
    }

    @Override
    public List<Room> findByRoomType(String roomType) {
        return roomRepository.findByRoomTypeAndActiveTrue(roomType);
    }

    @Override
    public List<Room> findByBuilding(String building) {
        return roomRepository.findByBuildingAndActiveTrue(building);
    }

    @Override
    public List<Room> findByFloor(Integer floor) {
        return roomRepository.findByFloorAndActiveTrue(floor);
    }

    @Override
    public List<Room> findRoomsWithMinCapacity(Integer minCapacity) {
        return roomRepository.findRoomsWithMinCapacity(minCapacity);
    }

    @Override
    public List<Room> findAvailableRooms(Integer dayOfWeek, Long timeSlotId) {
        return roomRepository.findAvailableRooms(dayOfWeek, timeSlotId);
    }

    @Override
    public List<Room> findByRoomNameContaining(String name) {
        return roomRepository.findByRoomNameContainingIgnoreCase(name);
    }

    @Override
    public boolean existsByRoomCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode).isPresent();
    }
}
