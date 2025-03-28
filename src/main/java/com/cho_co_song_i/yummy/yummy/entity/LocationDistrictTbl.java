package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.Date;

@Entity
@Table(name = "location_district_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDistrictTbl implements Persistable<LocationDistrictTblId> {
    @EmbeddedId
    private LocationDistrictTblId id;

    @Column(name = "location_district", nullable = false, length = 25)
    private String locationDistrict;

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
    public LocationDistrictTblId getId() { return this.id; }

    @Override
    public boolean isNew() { return isNew || this.id == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name="location_city_code", referencedColumnName="location_city_code", insertable = false, updatable = false),
            @JoinColumn(name="location_county_code", referencedColumnName="location_county_code", insertable = false, updatable = false)
    })
    private LocationCityTbl locationCity;
}