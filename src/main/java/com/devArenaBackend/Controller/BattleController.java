package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.BattleResultDTO;
import com.devArenaBackend.DTO.SubmissionDto;
import com.devArenaBackend.DTO.SubmissionResponse;
import com.devArenaBackend.Repository.ProblemRepository;
import com.devArenaBackend.Repository.RoomRepository;
import com.devArenaBackend.Repository.UserRepository;
import com.devArenaBackend.Service.DockerCodeExecution;
import com.devArenaBackend.Service.EloServices;
import com.devArenaBackend.entity.Room;
import com.devArenaBackend.entity.Status;
import com.devArenaBackend.entity.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller

public class BattleController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DockerCodeExecution dockerExecutionService;
    private final RoomRepository roomRepository;
    private final EloServices eloServices;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public BattleController(SimpMessagingTemplate messagingTemplate,
                            DockerCodeExecution dockerExecutionService,
                            RoomRepository roomRepository,
                            EloServices eloServices,
                            UserRepository userRepository,
                            ProblemRepository problemRepository) {
        this.messagingTemplate = messagingTemplate;
        this.dockerExecutionService = dockerExecutionService;
        this.roomRepository = roomRepository;
        this.eloServices = eloServices;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
    }

    @MessageMapping("/battle/{roomCode}/submit")
    public void handleSubmission(
            @DestinationVariable String roomCode,
            @Payload SubmissionDto submission
    ) {
        // Get the room to find the problem
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Get the problem and its test cases
        String testCasesJson = "[]";
        if (room.getProblemId() != null) {
            testCasesJson = problemRepository.findById(room.getProblemId())
                    .map(p -> p.getTestCases() != null ? p.getTestCases() : "[]")
                    .orElse("[]");
        }

        // Run code against test cases
        SubmissionResponse result = dockerExecutionService
                .executeWithTestCases(submission, testCasesJson);

        // Build result message
        BattleResultDTO message = new BattleResultDTO();
        message.setSubmitterEmail(submission.getSubmitterEmail());
        message.setSuccess(result.isSuccess());
        message.setOutput(result.getOutput());
        message.setError(result.getError());

        if (result.isSuccess() && room.getStatus() == Status.IN_PROGRESS) {
            room.setStatus(Status.FINISHED);
            room.setWinnerEmail(submission.getSubmitterEmail());
            roomRepository.save(room);

            String winnerEmail = submission.getSubmitterEmail();
            String loserEmail = room.getPlayer1Email().equals(winnerEmail)
                    ? room.getPlayer2Email()
                    : room.getPlayer1Email();

            int[] eloChanges = eloServices.updateElo(winnerEmail, loserEmail);

            message.setBattleOver(true);
            message.setWinnerEmail(winnerEmail);
            message.setWinnerEloChange(eloChanges[0]);
            message.setLoserEloChange(eloChanges[1]);
        } else {
            message.setBattleOver(false);
            message.setWinnerEmail(null);
        }

        messagingTemplate.convertAndSend("/topic/battle/" + roomCode, message);
    }
}