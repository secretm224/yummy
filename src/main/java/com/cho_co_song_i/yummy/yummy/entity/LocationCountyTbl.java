package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "location_county_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCountyTbl implements Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_county_code")
    private Long locationCountyCode;

    @Column(name = "location_county", nullable = false, length = 25)
    private String locationCounty;

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
    private boolean isNew = true;

    @Override
    public Long getId() {
        return this.locationCountyCode;
    }

    @Override
    public boolean isNew() {
        return isNew || this.locationCountyCode == null;
    }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() {
        this.isNew = true;
    }

    /* 관계 설정 */
    @OneToMany(mappedBy = "locationCounty", fetch = FetchType.LAZY)
    private List<LocationCityTbl> locationCities = new ArrayList<>();
}
