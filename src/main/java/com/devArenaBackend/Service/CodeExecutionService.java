package com.devArenaBackend.Service;

import com.devArenaBackend.DTO.SubmissionDto;
import com.devArenaBackend.DTO.SubmissionResponse;
//import com.devArenaBackend.entity.Language;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.devArenaBackend.DTO.Language;



@Service
public class CodeExecutionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Judge0 free public instance
    private static final String JUDGE0_URL = "https://ce.judge0.com";

    // Language IDs for Judge0
    // Full list at https://ce.judge0.com/languages
    private int getLanguageId(Language language) {
        return switch (language) {
            case JAVA -> 62;        // Java (OpenJDK 13.0.1)
            case PYTHON -> 71;      // Python (3.8.1)
            case JAVASCRIPT -> 63;  // JavaScript (Node.js 12.14.0)
        };
    }

    public SubmissionResponse execute(SubmissionDto submission) {
        return executeWithInput(submission, "");
    }

    public SubmissionResponse executeWithTestCases(
            SubmissionDto submission, String testCasesJson) {

        // Parse test cases
        List<Map<String, String>> testCases;
        try {
            testCases = objectMapper.readValue(
                    testCasesJson,
                    new com.fasterxml.jackson.core.type.TypeReference<>() {}
            );
        } catch (Exception e) {
            return execute(submission);
        }

        if (testCases.isEmpty()) {
            return execute(submission);
        }

        int passed = 0;
        int total = testCases.size();
        StringBuilder details = new StringBuilder();

        for (int i = 0; i < testCases.size(); i++) {
            Map<String, String> testCase = testCases.get(i);
            String input = testCase.get("input");
            String expectedOutput = testCase.get("expectedOutput").trim();

            SubmissionResponse result = executeWithInput(submission, input);
            String actualOutput = result.getOutput().trim();

            boolean testPassed = actualOutput.equals(expectedOutput);
            if (testPassed) passed++;

            details.append("Test ").append(i + 1).append(": ")
                    .append(testPassed ? "✅ Passed" : "❌ Failed")
                    .append("\n");

            if (!testPassed) {
                details.append("  Expected: ").append(expectedOutput).append("\n");
                details.append("  Got:      ").append(actualOutput).append("\n");
            }
        }

        SubmissionResponse response = new SubmissionResponse();
        response.setSuccess(passed == total);
        response.setOutput(details.toString());
        response.setError(passed == total ? "" : passed + "/" + total + " test cases passed");
        return response;
    }

    private SubmissionResponse executeWithInput(SubmissionDto submission, String input) {
        try {
            // Step 1 — Submit code to Judge0
            Map<String, Object> body = new HashMap<>();
            body.put("source_code", submission.getCode());
            body.put("language_id", getLanguageId(submission.getLanguage()));
            body.put("stdin", input);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Submit and get token
            ResponseEntity<String> submitResponse = restTemplate.postForEntity(
                    JUDGE0_URL + "/submissions?wait=true",
                    request,
                    String.class
            );

            // Step 2 — Parse result
            JsonNode result = objectMapper.readTree(submitResponse.getBody());

            SubmissionResponse response = new SubmissionResponse();

            // Status 3 = Accepted, others = error
            int statusId = result.path("status").path("id").asInt();

            if (statusId == 3) {
                // Success
                response.setSuccess(true);
                response.setOutput(result.path("stdout").asText(""));
                response.setError("");
            } else if (statusId == 6) {
                // Compilation error
                response.setSuccess(false);
                response.setOutput("");
                response.setError("Compilation Error:\n" +
                        result.path("compile_output").asText(""));
            } else if (statusId == 5) {
                // Time limit exceeded
                response.setSuccess(false);
                response.setOutput("");
                response.setError("Time Limit Exceeded");
            } else {
                // Runtime error or other
                response.setSuccess(false);
                response.setOutput("");
                response.setError("Runtime Error:\n" +
                        result.path("stderr").asText("Unknown error"));
            }

            return response;

        } catch (Exception e) {
            SubmissionResponse response = new SubmissionResponse();
            response.setSuccess(false);
            response.setError("Execution failed: " + e.getMessage());
            response.setOutput("");
            return response;
        }
    }
}