package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.RoomCreationDto;
import com.devArenaBackend.DTO.RoomJoinDto;
import com.devArenaBackend.Repository.RoomRepository;
import com.devArenaBackend.Service.RoomService;
import com.devArenaBackend.entity.Room;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
@CrossOrigin(origins="http://localhost:5173")
public class RoomController {
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    public RoomController(RoomService roomService,RoomRepository roomRepository) {
        this.roomService = roomService;
        this.roomRepository = roomRepository;
    }
    @GetMapping("/{roomCode}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .map(room -> new ResponseEntity<>(room, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody RoomCreationDto roomCreationDto) {
        Room room = roomService.createRoom(roomCreationDto.getEmail());
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @PostMapping("/join")
    public ResponseEntity<Room> joinRoom(@RequestBody RoomJoinDto roomJoinDto) {
        System.out.println(roomJoinDto.getRoomCode());
        System.out.println(roomJoinDto.getEmailPlayer2());
        Room room = roomService.joinRoom(roomJoinDto.getEmailPlayer2(), roomJoinDto.getRoomCode());
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }
}
