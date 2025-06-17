package com.cho_co_song_i.yummy.yummy.entity;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.dto.store.KakaoStoreDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor
public class Store implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "use_yn", length = 1, nullable = true, columnDefinition = "char(1) default 'Y'")
    private Character useYn;

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

    @Column(name = "tel", nullable = true, length = 20)
    private String tel;

    @Column(name = "url", nullable = true, length = 100)
    private String url;

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

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<StoreTypeLinkTbl> storeTypeLinks = new ArrayList<>();

//    @OneToOne(fetch = FetchType.LAZY, optional = true)
//    @JoinColumn(name="seq")
//    private StoreLocationInfoTbl storeLocations;

//    @OneToOne(fetch = FetchType.LAZY, optional = true)
//    @JoinColumn(name="seq")
//    private ZeroPossibleMarket zeroPossibles;

//    @OneToOne(fetch = FetchType.LAZY, optional = true)
//    @JoinColumn(name="seq")
//    private StoreLocationRoadInfoTbl storeLocationRoadInfos;

//    @OneToOne(mappedBy = "store", fetch = FetchType.LAZY)
//    @JoinColumn(name="seq")
//    private StoreCategoryTbl storeCategoryTbl;


    public Store(StoreDto dto) {
        this.name = dto.getName();
        this.type = dto.getType();
        this.useYn = dto.getUseYn();
        this.regDt = dto.getRegDt();
        this.regId = dto.getRegId();
        this.chgDt = dto.getChgDt();
        this.chgId = dto.getChgId();
        this.tel = dto.getTel();
        this.url = dto.getUrl();
    }

    public Store(AddStoreDto addStoreDto) {
        Instant nowInstant = Instant.now();

        this.name = addStoreDto.getName();
        this.type = addStoreDto.getType();
        this.useYn = 'Y';
        this.regDt = Date.from(nowInstant);
        this.regId = "system";
        this.tel = addStoreDto.getTel();
        this.url = addStoreDto.getUrl();
        markAsNew();
    }

    public Store(KakaoStoreDto kakaoStoreDto, String regId) {
        Instant nowInstant = Instant.now();

        this.name = kakaoStoreDto.getPlaceName();
        this.type = "store";
        this.useYn = 'Y';
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
        this.tel = kakaoStoreDto.getPhone();
        this.url = kakaoStoreDto.getPlaceUrl();
    }

    /**
     * StoreDto 의 데이터를 수정해주는 함수
     * @param dto
     * @param chgId
     */
    public void updateStore(StoreDto dto, String chgId) {

        Instant nowInstant = Instant.now();

        this.name = dto.getName();
        this.type = dto.getType();
        this.useYn = dto.getUseYn();
        this.regDt = dto.getRegDt();
        this.regId = dto.getRegId();
        this.chgDt = Date.from(nowInstant);
        this.chgId = chgId;
        this.tel = dto.getTel();
        this.url = dto.getUrl();
    }

    /**
     * 삼정의 연락처, url 을 update 해주는 함수
     * @param tel
     * @param url
     * @param chgId
     */
    public void updateContactInfo(String tel, String url, String chgId) {

        Instant nowInstant = Instant.now();

        this.tel = tel;
        this.url = url;
        this.chgId = chgId;
        this.chgDt = Date.from(nowInstant);
    }

    /**
     * KakaoStoreDto 기준으로 Store 데이터를 update 해주는 기능.
     * @param kakaoStoreDto
     * @param chgId
     */
    public void updateStoreFromKakaoStoreDto(KakaoStoreDto kakaoStoreDto, String chgId) {
        Instant nowInstant = Instant.now();

        this.name = kakaoStoreDto.getPlaceName();
        this.tel = kakaoStoreDto.getPhone();
        this.url = kakaoStoreDto.getPlaceUrl();
        this.chgId = chgId;
        this.chgDt = Date.from(nowInstant);
    }



}