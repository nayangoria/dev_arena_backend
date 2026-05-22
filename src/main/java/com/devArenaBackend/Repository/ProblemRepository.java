package com.devArenaBackend.Repository;

import com.devArenaBackend.entity.Difficulty;
import com.devArenaBackend.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface ProblemRepository extends JpaRepository<Problem,Long> {
    List<Problem> findByDifficulty(Difficulty difficulty);
    boolean existsByCodeforcesId(String codeforcesId);
    Optional<Problem> findByCodeforcesId(String codeforcesId);
    List<Problem> findAllByOrderByDifficultyRatingAsc();


}
