package com.devArenaBackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String roomCode;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String player1Email;
    private String player2Email;
    private Long problemId;
    private String winnerEmail;
}
