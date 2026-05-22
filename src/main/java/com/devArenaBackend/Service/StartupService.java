package com.devArenaBackend.Service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StartupService {

    private final LeaderboardService leaderboardService;

    public StartupService(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    // Runs automatically when Spring Boot starts
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        System.out.println("Rebuilding leaderboard from PostgreSQL...");
        leaderboardService.rebuildLeaderboard();
    }
}