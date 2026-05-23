package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.SubmissionDto;
import com.devArenaBackend.DTO.SubmissionResponse;
import com.devArenaBackend.Service.CodeExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submission")
@CrossOrigin(origins="http://localhost:5173")
public class SubmissionController {
    private final CodeExecutionService codeExecutionService;

    public SubmissionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public ResponseEntity<SubmissionResponse> run(@RequestBody SubmissionDto submissionDto) {
        return new ResponseEntity<>(
                codeExecutionService.execute(submissionDto),
                HttpStatus.OK
        );
    }
}
