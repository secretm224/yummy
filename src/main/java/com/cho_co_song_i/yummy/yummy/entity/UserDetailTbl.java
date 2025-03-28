package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.Date;



@Entity
@Table(name = "user_detail_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailTbl implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_no")
    private Long detailNo;

    @ManyToOne
    @JoinColumn(name="user_no")
    private UserTbl user;

    @Column(name = "user_no", nullable = false, insertable = false, updatable = false)
    private Long userNo;

    @Column(name = "addr_type", length = 2, nullable = false)
    private String addrType;

    @Column(name = "addr", length = 255, nullable = false)
    private String addr;

    @Column(name = "lng_x", precision = 10, scale = 7, nullable = false)
    private BigDecimal lngX;

    @Column(name = "lat_y", precision = 10, scale = 7, nullable = false)
    private BigDecimal latY;

    @Column(name = "reg_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date regDt;

    @Column(name = "reg_id", length = 25)
    private String regId;

    @Column(name = "chg_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chgDt;

    @Column(name = "chg_id", length = 25)
    private String chgId;

    @Transient
    private boolean isNew = true;

    public Long getId(){
        return this.detailNo;
    }

    public boolean isNew(){
        return this.detailNo == null || isNew;
    }

    public void markNotNew(){
        this.isNew = false;
    }
}
