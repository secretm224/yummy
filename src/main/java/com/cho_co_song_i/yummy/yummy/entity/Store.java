package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store") // 가게 테이블 이름. 필요하면 변경해줘.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "use_yn", length = 1, nullable = true, columnDefinition = "char(1) default 'Y'")
    private String useYn;

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

// lombok 에서 자동 생성 처리 된다.
//    public Long getSeq() {
//        return seq;
//    }
//
//    public void setSeq(Long seq) {
//        this.seq = seq;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getUseYn() {
//        return useYn;
//    }
//
//    public void setUseYn(String useYn) {
//        this.useYn = useYn;
//    }
//
//    public Date getRegDt() {
//        return regDt;
//    }
//
//    public void setRegDt(Date regDt) {
//        this.regDt = regDt;
//    }
//
//    public String getRegId() {
//        return regId;
//    }
//
//    public void setRegId(String regId) {
//        this.regId = regId;
//    }
//
//    public Date getChgDt() {
//        return chgDt;
//    }
//
//    public void setChgDt(Date chgDt) {
//        this.chgDt = chgDt;
//    }
//
//    public String getChgId() {
//        return chgId;
//    }
//
//    public void setChgId(String chgId) {
//        this.chgId = chgId;
//    }
}