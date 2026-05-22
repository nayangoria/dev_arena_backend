package com.devArenaBackend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ProblemUpdateDto {
    private String codeforcesId;
    private String description;
    private List<TestCaseDto> testCases;

    @Data
    public static class TestCaseDto {
        private String input;
        private String expectedOutput;
    }
}
