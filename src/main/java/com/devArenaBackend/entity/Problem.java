package com.devArenaBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="problems")
public class Problem {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;
    private String codeforcesId;      // e.g. "1A", "1B"
    private Integer difficultyRating; // actual CF rating like 1200
    private String tags;
    @Column(columnDefinition = "TEXT")
    private String testCases;
    private LocalDateTime CreatedAt;
    @PrePersist
    public void prePersist(){
        CreatedAt = LocalDateTime.now();
    }
}
