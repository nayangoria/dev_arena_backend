package com.devArenaBackend.Service;

import com.devArenaBackend.Repository.UserRepository;
import com.devArenaBackend.entity.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;


    // This is the key we use in Redis sorted set
    private static final String LEADERBOARD_KEY = "devArena:leaderboard";

    public LeaderboardService(RedisTemplate<String, String> redisTemplate,
                              UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    // Called after every battle to update Redis
    public void updateScore(String email, int eloRating) {
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, email, eloRating);
    }

    // Get top 10 players from Redis
    public List<LeaderboardEntry> getTopPlayers() {
        // ZREVRANGE — get emails sorted by score descending (highest first)
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> topPlayers =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(LEADERBOARD_KEY, 0, 9);

        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        if (topPlayers == null) return leaderboard;

        int rank = 1;
        for (var entry : topPlayers) {
            String email = entry.getValue();
            int score = entry.getScore().intValue();

            // Get name from PostgreSQL
            String name = userRepository.findByEmail(email)
                    .map(User::getName)
                    .orElse("Unknown");

            leaderboard.add(new LeaderboardEntry(rank, name, email, score));
            rank++;
        }

        return leaderboard;
    }

    // Load all users from PostgreSQL into Redis
    // Call this once on startup to populate Redis
    public void rebuildLeaderboard() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            redisTemplate.opsForZSet().add(
                    LEADERBOARD_KEY,
                    user.getEmail(),
                    user.getEloRating()
            );
        }
        System.out.println("Leaderboard rebuilt with " + allUsers.size() + " users");
    }

    // Simple record to hold leaderboard entry
    public record LeaderboardEntry(int rank, String name, String email, int eloRating) {}
}
