package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity // TODO: indexes
@Table(name = "user_badges", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_badge", columnNames = {"user_id", "badge_id"})}) //한 user당 같은 뱃지 1회만 지급
public class UserBadge extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 직접 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 직접 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE NOT NULL")
    private Boolean deleted = false;

    @PrePersist
    protected void init(){
        if (deleted == null) deleted = false;
    }

}
