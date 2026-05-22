package com.devArenaBackend.Service;

import com.devArenaBackend.Repository.UserRepository;
import com.devArenaBackend.entity.User;
import org.springframework.stereotype.Service;

@Service
public class EloServices {
    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;
    private static final int K = 32;

    EloServices(UserRepository userRepository,LeaderboardService leaderboardService) {
        this.userRepository = userRepository;
        this.leaderboardService = leaderboardService;
    }
    public int[] updateElo(String winnerEmail,String loserEmail){
        User winner = userRepository.findByEmail(winnerEmail).orElseThrow(()-> new RuntimeException("player not found"));
        User loser=userRepository.findByEmail(loserEmail).orElseThrow(()-> new RuntimeException("player 2 not found"));

        double winnerRating = winner.getEloRating();
        double loserRating = loser.getEloRating();

        // Calculate expected scores
        double expectedWinner = 1.0 / (1 + Math.pow(10, (loserRating - winnerRating) / 400));
        double expectedLoser = 1.0 / (1 + Math.pow(10, (winnerRating - loserRating) / 400));

        // Calculate new ratings
        int newWinnerRating = (int) Math.round(winnerRating + K * (1 - expectedWinner));
        int newLoserRating = (int) Math.round(loserRating + K * (0 - expectedLoser));

        // Minimum rating is 0 — can't go below 0
        newLoserRating = Math.max(0, newLoserRating);
        int winnerChange = newWinnerRating - (int) winnerRating;
        int loserChange = newLoserRating - (int) loserRating;

        // Save new ratings
        winner.setEloRating(newWinnerRating);
        loser.setEloRating(newLoserRating);

        userRepository.save(winner);
        userRepository.save(loser);

        System.out.println("ELO updated — Winner: " + winnerEmail + " → " + newWinnerRating);
        System.out.println("ELO updated — Loser: " + loserEmail + " → " + newLoserRating);
        leaderboardService.updateScore(winnerEmail, newWinnerRating);
        leaderboardService.updateScore(loserEmail, newLoserRating);
        return new int[]{winnerChange, loserChange};
    }
}
