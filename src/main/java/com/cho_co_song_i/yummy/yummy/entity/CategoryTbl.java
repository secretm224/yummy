package com.cho_co_song_i.yummy.yummy.entity;

import com.cho_co_song_i.yummy.yummy.dto.store.KakaoStoreDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "category_tbl")
@Getter
@NoArgsConstructor
public class CategoryTbl implements Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_seq")
    private Long categorySeq;

    @Column(name = "category_group_code", nullable = false, length = 25)
    private String categoryGroupCode;

    @Column(name = "category_group_name", nullable = false, length = 30)
    private String categoryGroupName;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "category_icon", nullable = true, length = 100)
    private String categoryIcon;

    @Column(name = "reg_dt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date regDt;

    @Column(name = "reg_id", nullable = false, length = 25)
    private String regId;

    @Column(name = "chg_dt", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date chgDt;

    @Column(name = "chg_id", nullable = true, length = 25)
    private String chgId;

    @OneToMany(mappedBy = "categoryTbl", fetch = FetchType.LAZY)
    private List<StoreCategoryTbl> storeCategoryTbls = new ArrayList<>();

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public Long getId() {
        return this.categorySeq;
    }

    @Override
    public boolean isNew() {
        return isNew || this.categorySeq == null;
    }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() {
        this.isNew = true;
    }

    public CategoryTbl(KakaoStoreDto kakaoStoreDto, String regId) {
        Instant nowInstant = Instant.now();

        this.categoryGroupCode = kakaoStoreDto.getCategoryGroupCode();
        this.categoryGroupName = kakaoStoreDto.getCategoryGroupName();
        this.categoryName = kakaoStoreDto.getCategoryName();
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
    }
}
