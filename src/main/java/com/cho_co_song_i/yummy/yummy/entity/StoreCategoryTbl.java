package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "store_category_tbl")
@Getter
@NoArgsConstructor
public class StoreCategoryTbl implements Persistable<StoreCategoryTblId> {
    @EmbeddedId
    private StoreCategoryTblId id;

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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("seq") /* EmbeddedId 필드명 */
    @JoinColumn(name = "seq") /* 실제 DB 컬럼명 */
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_seq", referencedColumnName = "category_seq", insertable = false, updatable = false)
    private CategoryTbl categoryTbl;

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public StoreCategoryTblId getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return isNew || this.id == null;
    }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() {
        this.isNew = true;
    }


    public StoreCategoryTbl(Store store, CategoryTbl categoryTbl, String regId) {
        Instant nowInstant = Instant.now();

        StoreCategoryTblId storeCategoryTblId = new StoreCategoryTblId(store.getSeq(), categoryTbl.getCategorySeq());
        this.store = store;
        this.categoryTbl = categoryTbl;
        this.id = storeCategoryTblId;
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
    }
}