package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.SubmissionDto;
import com.devArenaBackend.DTO.SubmissionResponse;
import com.devArenaBackend.Repository.ProblemRepository;
import com.devArenaBackend.Service.CodeExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submission")
@CrossOrigin(origins="http://localhost:5173")
public class SubmissionController {
    private final CodeExecutionService codeExecutionService;
    private final ProblemRepository problemRepository;

    public SubmissionController(CodeExecutionService codeExecutionService,ProblemRepository problemRepository) {
        this.codeExecutionService = codeExecutionService;
        this.problemRepository = problemRepository;
    }

    @PostMapping("/run")
    public ResponseEntity<SubmissionResponse> run(@RequestBody SubmissionDto submissionDto) {
        return new ResponseEntity<>(
                codeExecutionService.execute(submissionDto),
                HttpStatus.OK
        );
    }
    @PostMapping("/run-with-tests/{problemId}")
    public ResponseEntity<SubmissionResponse> runWithTests(
            @PathVariable Long problemId,
            @RequestBody SubmissionDto submissionDto) {

        // Get problem test cases
        String testCasesJson = problemRepository.findById(problemId)
                .map(p -> p.getTestCases() != null ? p.getTestCases() : "[]")
                .orElse("[]");

        return new ResponseEntity<>(
                codeExecutionService.executeWithTestCases(submissionDto, testCasesJson),
                HttpStatus.OK
        );
    }
}
