package com.devArenaBackend.DTO;

import lombok.Data;

@Data
public class SubmissionDto {
    private String code;
    private Language language;
    private String submitterEmail;
}
