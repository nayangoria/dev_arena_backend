package com.devArenaBackend.Service;

import com.devArenaBackend.DTO.ProblemUpdateDto;
import com.devArenaBackend.Repository.ProblemRepository;
import com.devArenaBackend.entity.Difficulty;
import com.devArenaBackend.entity.Problem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CodeforcesService {

    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private static final String CF_API_URL =
            "https://codeforces.com/api/problemset.problems?tags=implementation";
    private static final int MAX_PROBLEMS = 300;

    public CodeforcesService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    public int fetchAndSaveProblems() {
        try {
            // Call Codeforces API
            String response = restTemplate.getForObject(CF_API_URL, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode problems = root.path("result").path("problems");

            int count = 0;
            List<Problem> toSave = new ArrayList<>();

            for (JsonNode node : problems) {
                if (count >= MAX_PROBLEMS) break;

                // Skip problems without rating
                if (!node.has("rating")) continue;

                int rating = node.get("rating").asInt();
                String contestId = node.get("contestId").asText();
                String index = node.get("index").asText();
                String codeforcesId = contestId + index;
                String title = node.get("name").asText();

                // Skip if already exists
                if (problemRepository.existsByCodeforcesId(codeforcesId)) continue;

                // Map rating to difficulty
                Difficulty difficulty = mapDifficulty(rating);

                // Get tags
                StringBuilder tags = new StringBuilder();
                for (JsonNode tag : node.path("tags")) {
                    if (tags.length() > 0) tags.append(",");
                    tags.append(tag.asText());
                }

                // Create problem
                Problem problem = new Problem();
                problem.setTitle(title);
                problem.setCodeforcesId(codeforcesId);
                problem.setDifficultyRating(rating);
                problem.setDifficulty(difficulty);
                problem.setTags(tags.toString());
                problem.setDescription("Description coming soon for: " + title);
                problem.setTestCases("[]");

                toSave.add(problem);
                count++;
            }

            // Save all at once
            problemRepository.saveAll(toSave);
            return toSave.size();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch from Codeforces: " + e.getMessage());
        }
    }

    private Difficulty mapDifficulty(int rating) {
        if (rating <= 1200) return Difficulty.EASY;
        if (rating <= 1999) return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }
    public int updateProblemsFromJson(List<ProblemUpdateDto> updates) {
        int count = 0;
        for (ProblemUpdateDto update : updates) {
            // Find by codeforcesId directly
            Optional<Problem> existing = problemRepository
                    .findByCodeforcesId(update.getCodeforcesId());

            if (existing.isPresent()) {
                Problem problem = existing.get();
                problem.setDescription(update.getDescription());

                try {
                    String testCasesJson = objectMapper
                            .writeValueAsString(update.getTestCases());
                    problem.setTestCases(testCasesJson);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert: " + e.getMessage());
                }

                problemRepository.save(problem);
                count++;
                System.out.println("Updated: " + update.getCodeforcesId());
            } else {
                System.out.println("Not found: " + update.getCodeforcesId());
            }
        }
        return count;
    }
}