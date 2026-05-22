package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.ProblemUpdateDto;
import com.devArenaBackend.Service.CodeforcesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
//@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final CodeforcesService codeforcesService;

    public AdminController(CodeforcesService codeforcesService) {
        this.codeforcesService = codeforcesService;
    }

    @PostMapping("/fetch-problems")
    public ResponseEntity<?> fetchProblems() {
        int count = codeforcesService.fetchAndSaveProblems();
        return ResponseEntity.ok(Map.of(
                "message", "Successfully fetched problems",
                "count", count
        ));
    }
    @PostMapping("/upload-problems")
    public ResponseEntity<?> uploadProblems(@RequestBody List<ProblemUpdateDto> updates) {
        int count = codeforcesService.updateProblemsFromJson(updates);
        return ResponseEntity.ok(Map.of(
                "message", "Successfully updated problems",
                "count", count
        ));
    }
}
