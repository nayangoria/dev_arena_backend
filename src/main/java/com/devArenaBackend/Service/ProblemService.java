package com.devArenaBackend.Service;

import com.devArenaBackend.Repository.ProblemRepository;
import com.devArenaBackend.entity.Problem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;
    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }
    public List<Problem> findAll(){
        return problemRepository.findAllByOrderByDifficultyRatingAsc();
    }
    public Optional<Problem> findById(Long id){
        return problemRepository.findById(id);
    }
    public Problem save(Problem problem){
        return problemRepository.save(problem);
    }

}
