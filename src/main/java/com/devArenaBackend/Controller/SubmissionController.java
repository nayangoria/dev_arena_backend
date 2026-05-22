package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.SubmissionDto;
import com.devArenaBackend.DTO.SubmissionResponse;
import com.devArenaBackend.Service.DockerCodeExecution;
import com.github.dockerjava.api.DockerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submission")
@CrossOrigin(origins="http://localhost:5173")
public class SubmissionController {
    private final DockerCodeExecution dockerCodeExecution;
     public SubmissionController(DockerCodeExecution dockerCodeExecution) {
         this.dockerCodeExecution = dockerCodeExecution;
     }
    @PostMapping("/run")
    public ResponseEntity<SubmissionResponse> run(@RequestBody SubmissionDto submissionDto) {
         return new ResponseEntity<>(dockerCodeExecution.execute(submissionDto), HttpStatus.OK);
    }
}
