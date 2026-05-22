package com.devArenaBackend.DTO;

import lombok.Data;

@Data
public class BattleResultDTO {
    private String submitterEmail;
    private boolean success;
    private String output;
    private String error;
    private boolean battleOver;
    private String winnerEmail;
    private int winnerEloChange;
    private int loserEloChange;
}

