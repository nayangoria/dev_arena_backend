package com.devArenaBackend.Service;

import com.devArenaBackend.DTO.SubmissionDto;
import com.devArenaBackend.DTO.SubmissionResponse;
import com.devArenaBackend.DTO.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DockerCodeExecution {

    // Connect to Docker running on your machine
    @Value("${docker.host:tcp://localhost:2375}")
    private String dockerHost;
    private  DockerClient dockerClient;
    @PostConstruct
    public void init() {
        var config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public SubmissionResponse execute(SubmissionDto submission) {
        // Each submission gets a unique folder so they don't interfere
//        List<Map<String,String>> testcase;
        String submissionId = UUID.randomUUID().toString();
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), submissionId);

        try {
            // Step 1 — Create a temp folder for this submission
            Files.createDirectories(tempDir);

            // Step 2 — Write the code to a file
            String filename = getFilename(submission.getLanguage());
            Path codeFile = tempDir.resolve(filename);
            Files.writeString(codeFile, submission.getCode());

            // Step 3 — Pick the right Docker image and run command
            String image = getDockerImage(submission.getLanguage());
            String[] command = getRunCommand(submission.getLanguage(), filename);

            // Step 4 — Create the container
            CreateContainerResponse container = dockerClient
                    .createContainerCmd(image)
                    .withHostConfig(HostConfig.newHostConfig()
                            // Mount our temp folder into the container
                            .withBinds(Bind.parse(tempDir.toAbsolutePath() + ":/code"))
                            // Limit memory to 128MB for safety
                            .withMemory(128 * 1024 * 1024L)
                            // Limit CPU
                            .withNanoCPUs(500_000_000L)
                    )
                    .withWorkingDir("/code")
                    .withCmd(command)
                    .exec();

            // Step 5 — Start the container
            dockerClient.startContainerCmd(container.getId()).exec();

            // Step 6 — Wait for it to finish (max 10 seconds)
            dockerClient.waitContainerCmd(container.getId())
                    .start()
                    .awaitCompletion(10, java.util.concurrent.TimeUnit.SECONDS);

            // Step 7 — Capture the output
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            dockerClient.logContainerCmd(container.getId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(com.github.dockerjava.api.model.Frame frame) {
                            String log = new String(frame.getPayload());
                            if (frame.getStreamType().name().equals("STDERR")) {
                                error.append(log);
                            } else {
                                output.append(log);
                            }
                        }
                    }).awaitCompletion();

            // Step 8 — Delete the container
            dockerClient.removeContainerCmd(container.getId())
                    .withForce(true)
                    .exec();

            // Step 9 — Build and return response
            SubmissionResponse response = new SubmissionResponse();
            if (error.length() > 0) {
                response.setSuccess(false);
                response.setError(error.toString());
                response.setOutput("");
            } else {
                response.setSuccess(true);
                response.setOutput(output.toString());
                response.setError("");
            }
            return response;

        } catch (Exception e) {
            SubmissionResponse response = new SubmissionResponse();
            response.setSuccess(false);
            response.setError("Execution failed: " + e.getMessage());
            response.setOutput("");
            return response;
        } finally {
            // Always clean up temp files
            deleteDirectory(tempDir.toFile());
        }
    }

    // What file name to use for each language
    private String getFilename(Language language) {
        return switch (language) {
            case JAVA -> "Solution.java";
            case PYTHON -> "solution.py";
            case JAVASCRIPT -> "solution.js";
        };
    }

    // Which Docker image to use for each language
    private String getDockerImage(Language language) {
        return switch (language) {
            case JAVA -> "eclipse-temurin:21-alpine";
            case PYTHON -> "python:3.11-alpine";
            case JAVASCRIPT -> "node:18-alpine";
        };
    }

    // How to run the code in each language
    private String[] getRunCommand(Language language, String filename) {
        return switch (language) {
            case JAVA -> new String[]{"sh", "-c", "javac " + filename + " && java Solution"};
            case PYTHON -> new String[]{"python3", filename};
            case JAVASCRIPT -> new String[]{"node", filename};
        };
    }

    // Delete temp folder after execution
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        dir.delete();
    }

    public SubmissionResponse executeWithTestCases(SubmissionDto submission, String testCasesJson) {
        // Parse test cases
        List<Map<String, String>> testCases;
        try {
            testCases = new ObjectMapper().readValue(
                    testCasesJson,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {}
            );
        } catch (Exception e) {
            SubmissionResponse response = new SubmissionResponse();
            response.setSuccess(false);
            response.setError("Invalid test cases format");
            return response;
        }

        if (testCases.isEmpty()) {
            // No test cases — just run normally
            return execute(submission);
        }

        int passed = 0;
        int total = testCases.size();
        StringBuilder details = new StringBuilder();

        for (int i = 0; i < testCases.size(); i++) {
            Map<String, String> testCase = testCases.get(i);
            String input = testCase.get("input");
            String expectedOutput = testCase.get("expectedOutput").trim();

            // Run code with this input
            String actualOutput = runWithInput(submission, input).trim();

            boolean testPassed = actualOutput.equals(expectedOutput);
            if (testPassed) passed++;

            details.append("Test ").append(i + 1).append(": ")
                    .append(testPassed ? "✅ Passed" : "❌ Failed")
                    .append("\n");

            if (!testPassed) {
                details.append("  Expected: ").append(expectedOutput).append("\n");
                details.append("  Got: ").append(actualOutput).append("\n");
            }
        }

        SubmissionResponse response = new SubmissionResponse();
        response.setSuccess(passed == total);
        response.setOutput(details.toString());
        response.setError(passed == total ? "" : passed + "/" + total + " test cases passed");
        return response;
    }

    private String runWithInput(SubmissionDto submission, String input) {
        String submissionId = UUID.randomUUID().toString();
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), submissionId);

        try {
            Files.createDirectories(tempDir);

            String filename = getFilename(submission.getLanguage());
            Path codeFile = tempDir.resolve(filename);
            Files.writeString(codeFile, submission.getCode());

            // Write input to a file
            Path inputFile = tempDir.resolve("input.txt");
            Files.writeString(inputFile, input);

            String image = getDockerImage(submission.getLanguage());
            String[] command = getRunCommandWithInput(submission.getLanguage(), filename);

            CreateContainerResponse container = dockerClient
                    .createContainerCmd(image)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withBinds(Bind.parse(tempDir.toAbsolutePath() + ":/code"))
                            .withMemory(128 * 1024 * 1024L)
                            .withNanoCPUs(500_000_000L)
                    )
                    .withWorkingDir("/code")
                    .withCmd(command)
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();
            dockerClient.waitContainerCmd(container.getId())
                    .start()
                    .awaitCompletion(10, java.util.concurrent.TimeUnit.SECONDS);

            StringBuilder output = new StringBuilder();
            dockerClient.logContainerCmd(container.getId())
                    .withStdOut(true)
                    .withStdErr(false)
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(com.github.dockerjava.api.model.Frame frame) {
                            output.append(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();

            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
            return output.toString();

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    private String[] getRunCommandWithInput(Language language, String filename) {
        return switch (language) {
            case JAVA -> new String[]{"sh", "-c",
                    "javac " + filename + " && java Solution < input.txt"};
            case PYTHON -> new String[]{"sh", "-c",
                    "python3 " + filename + " < input.txt"};
            case JAVASCRIPT -> new String[]{"sh", "-c",
                    "node " + filename + " < input.txt"};
        };
    }
}
