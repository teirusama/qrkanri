package org.qrkanri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String duty;
    private String phone;
    private String email;

    @Column(unique = true)
    private String loginid;
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "late_count")
    private int lateCount;

    @Column(nullable = false)
    private boolean active;

    private String role;
}
