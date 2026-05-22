package com.devArenaBackend.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class healthCheck {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "DevArena Backend");
    }

}
