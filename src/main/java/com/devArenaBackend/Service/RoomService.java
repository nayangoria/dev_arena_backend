package com.devArenaBackend.Service;

import com.devArenaBackend.DTO.RoomUpdateDTO;
import com.devArenaBackend.Repository.ProblemRepository;
import com.devArenaBackend.Repository.RoomRepository;
import com.devArenaBackend.entity.Problem;
import com.devArenaBackend.entity.Room;
import com.devArenaBackend.entity.Status;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ProblemRepository problemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    RoomService(RoomRepository roomRepository, ProblemRepository problemRepository,SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.problemRepository = problemRepository;
        this.messagingTemplate=messagingTemplate;
    }

    public Room createRoom(String emailPlayer1) {
        Room room = new Room();
        String roomCode= UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        List<Long> problemIds = problemRepository.findAll().stream().map(p->p.getId()).toList();
        if(problemIds.isEmpty()){
            throw new RuntimeException("There are no problems");
        }
        int randomIndex = (int) (Math.random() * problemIds.size());
        Long problemId = problemIds.get(randomIndex);
        room.setProblemId(problemId);
        room.setRoomCode(roomCode);
        room.setPlayer1Email(emailPlayer1);
        room.setStatus(Status.WAITING);
        roomRepository.save(room);
        return room;
    }
    public Room joinRoom(String emailPlayer2,String roomCode) {

        System.out.println(roomCode);
        Room room = roomRepository.findByRoomCode(roomCode).orElseThrow(()-> new RuntimeException("Room code not found"));
        if(room.getStatus()!=Status.WAITING){
            throw new RuntimeException("Room code is not available");
        }
        System.out.println("Setting player2 email: " + emailPlayer2);
        room.setPlayer2Email(emailPlayer2);
        room.setStatus(Status.IN_PROGRESS);
        roomRepository.save(room);
        RoomUpdateDTO update = new RoomUpdateDTO();
        update.setPlayer1Email(room.getPlayer1Email());
        update.setPlayer2Email(emailPlayer2);
        update.setStatus("IN_PROGRESS");

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                update
        );
        return room;
    }
}