package com.devArenaBackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long Id;
    @Column(unique=true,nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private int eloRating=1000;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role=Role.USER;

}
