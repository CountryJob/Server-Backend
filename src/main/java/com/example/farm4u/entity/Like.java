package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Entity
@Table(name = "likes",
        uniqueConstraints = {@UniqueConstraint(name = "unique_like", columnNames = {"user_id", "job_id"})}) // 한 worker당 job에 하나의 like 가능
public class Like extends BaseEntity { // TODO: indexes

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드만 (like를 누른 사용자(worker))
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 필드만 (like 대상 공고)
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void init() {
        if (deleted == null) this.deleted = false;
    }

    public void setDeleted(Boolean deleted){
        this.deleted = deleted;
    }

}
