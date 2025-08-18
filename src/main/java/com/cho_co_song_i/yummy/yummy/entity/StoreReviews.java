package com.cho_co_song_i.yummy.yummy.entity;

import com.cho_co_song_i.yummy.yummy.dto.store.VisitTypeConverter;
import com.cho_co_song_i.yummy.yummy.enums.VisitType;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_reviews")
public class StoreReviews implements Persistable<StoreReviewId>  {
    @EmbeddedId
    private StoreReviewId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Reviews review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seq", insertable = false, updatable = false)
    private Store store;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Convert(converter = VisitTypeConverter.class)
    @Column(name = "visit_type", nullable = false)
    private VisitType visitType = VisitType.DINE_IN;

    @Column(name = "visit_cnt", nullable = false)
    private Integer visitCnt;

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
    public StoreReviewId getId() { return this.id; }

    @Override
    public boolean isNew() { return isNew || this.id == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }

}
