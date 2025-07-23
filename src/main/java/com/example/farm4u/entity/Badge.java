package com.example.farm4u.entity;
import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(name = "badges")
@Getter @NoArgsConstructor
@AllArgsConstructor @Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;            // 뱃지명

    @Column(name = "img_url", nullable = false, length = 255)
    private String imgUrl;           // 뱃지 이미지 경로

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 50)
    private BadgeType badgeType;     // 뱃지 유형(ENUM)

    @Column(length = 255)
    private String condition;        // 지급 조건 설명

    public enum BadgeType {
        PUNCTUALITY, REHIRE, DILIGENCE, ALL_ROUNDER, VETERAN
    }
}
