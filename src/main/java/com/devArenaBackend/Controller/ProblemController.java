package com.devArenaBackend.Controller;

import com.devArenaBackend.Repository.ProblemRepository;
import com.devArenaBackend.Service.ProblemService;
import com.devArenaBackend.entity.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
//@CrossOrigin(origins = "http://localhost:5173")
public class ProblemController {
    private final ProblemService problemService;
    ProblemController(ProblemService problemService) {
        this .problemService = problemService;
    }
    @GetMapping
    public ResponseEntity<List<Problem>> getAllProblems() {
        return new ResponseEntity<>(problemService.findAll(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProblemById(@PathVariable long id) {
        return problemService.findById(id)
                .map(problem -> new ResponseEntity<>(problem, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PostMapping
    public ResponseEntity<?> saveProblem(@RequestBody Problem problem) {
        return new ResponseEntity<>(problemService.save(problem),HttpStatus.CREATED);
    }
}
