package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor @Builder @Setter
@NoArgsConstructor
@Entity
@Table(name = "reports") // TODO: indexes
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드만 (신고자)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    // farmer -> worker 신고일 때만 값 있음(나머지 null)
    @Column(name = "worker_user_id")
    private Long workerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('REPORTED', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'REPORTED'")
    private State state;

    public enum State{REPORTED, ACCEPTED, REJECTED}

    public enum Reason {
        ABSENCE, POOR_PERFORMANCE, BAD_MANNER, FALSE_INFO,
        VERBAL_ABUSE, INACCURATE_INFO, UNPAID, OTHER_MISCONDUCT
    }

    @PrePersist
    protected void init(){
        if (state == null) state = State.REPORTED;
    }
}
