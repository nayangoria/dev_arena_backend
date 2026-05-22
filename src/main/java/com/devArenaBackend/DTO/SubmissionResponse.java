package com.devArenaBackend.DTO;

import lombok.Data;

@Data
public class SubmissionResponse {
    private String output;
    private String error;
    private boolean success;
}
