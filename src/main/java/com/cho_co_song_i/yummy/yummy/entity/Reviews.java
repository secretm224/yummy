package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Reviews implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "rating", nullable = false)
    private Short rating; /* TINYINT UNSIGNED → 0~255, 도메인 제약은 서비스/검증에서 1~5로 */

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "photos_count", nullable = false)
    private Integer photosCount = 0;

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;

    @Column(name = "reported_count", nullable = false)
    private Integer reportedCount = 0;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "chg_dt")
    private LocalDateTime chgDt;

    @Column(name = "reg_id", nullable = false, length = 25)
    private String regId;

    @Column(name = "chg_id", length = 25)
    private String chgId;

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public Long getId() { return this.reviewId; }

    @Override
    public boolean isNew() { return isNew || this.reviewId == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
    @OrderBy("isPrimary DESC, sortOrder ASC, photoId ASC")
    private List<ReviewPhotos> photos = new ArrayList<>();
}
