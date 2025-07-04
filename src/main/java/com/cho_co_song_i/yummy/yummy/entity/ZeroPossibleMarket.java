package com.cho_co_song_i.yummy.yummy.entity;

import com.cho_co_song_i.yummy.yummy.dto.store.KakaoStoreDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "zero_possible_market")
@NoArgsConstructor
@Getter
public class ZeroPossibleMarket implements Persistable<Long> {
    @Id
    @Column(name = "seq")
    private Long seq;

    @Column(name = "use_yn", nullable = true, columnDefinition = "char(1)")
    private Character useYn;
    
    // 해당 필드는 필요없어 보임 //
    @Column(name = "name", nullable = true, length = 255)
    private String name;

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

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public Long getId() {
        return this.seq;
    }

    @Override
    public boolean isNew() {
        return isNew || this.seq == null;
    }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() {
        this.isNew = true;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq")
    private Store store;

    public ZeroPossibleMarket(Store store, String regId) {
        Instant nowInstant = Instant.now();

        this.useYn = 'Y';
        this.name = store.getName();
        this.seq = store.getSeq();
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
    }
}
