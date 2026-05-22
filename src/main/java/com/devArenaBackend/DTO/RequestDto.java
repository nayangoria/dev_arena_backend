package com.devArenaBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestDto {
    String name;
    String email;
    String password;
    private String adminSecretKey;
}
