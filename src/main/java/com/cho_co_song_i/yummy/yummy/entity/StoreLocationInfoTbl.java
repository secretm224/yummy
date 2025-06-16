package com.cho_co_song_i.yummy.yummy.entity;

import com.cho_co_song_i.yummy.yummy.dto.store.KakaoStoreDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "store_location_info_tbl")
@Getter
@NoArgsConstructor
public class StoreLocationInfoTbl implements Persistable<Long> {

    @Id
    @Column(name = "seq")
    private Long seq;

    @Column(name = "lat", precision = 20, scale = 16, nullable = false)
    private BigDecimal lat;

    @Column(name = "lng", precision = 20, scale = 16, nullable = false)
    private BigDecimal lng;

    @Column(name = "address", nullable = true, length = 500)
    private String address;

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

    @OneToOne(mappedBy = "storeLocations", fetch = FetchType.LAZY)
    private Store store;

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


    public StoreLocationInfoTbl(KakaoStoreDto kakaoStoreDto, Store store, String regId) {
        Instant nowInstant = Instant.now();

        this.store = store;
        this.seq = store.getSeq();
        this.lat = kakaoStoreDto.getLat();
        this.lng = kakaoStoreDto.getLng();
        this.address = kakaoStoreDto.getAddressName();
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
    }

}
