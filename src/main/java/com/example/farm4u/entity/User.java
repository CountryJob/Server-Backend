package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor @Builder
@NoArgsConstructor
@Getter
@Entity
@Table(name = "users") // TODO: indexes
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", length = 20, nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_mode", nullable = false)
    private Mode currentMode = Mode.ANONYMOUS;

    @Column(nullable = false)
    private Boolean deleted = false;

    public enum Mode {
        WORKER, FARMER, ADMIN, ANONYMOUS;
    }
}
