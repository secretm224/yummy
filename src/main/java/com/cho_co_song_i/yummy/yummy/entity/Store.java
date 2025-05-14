package com.cho_co_song_i.yummy.yummy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "store")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="seq")
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<StoreLocationInfoTbl> storeLocations = new ArrayList<>();

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="seq")
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<ZeroPossibleMarket> zeroPossibles = new ArrayList<>();
}