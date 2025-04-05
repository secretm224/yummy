package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.Date;

@Entity
@Table(name = "user_auth_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthTbl implements Persistable<UserAuthTblId> {

    @EmbeddedId
    private UserAuthTblId id;

    @Column(name="reg_dt",nullable = false)
    private Date reg_dt;

    @Column(name="reg_id",length = 25,nullable = false)
    private String reg_id;

    @Column(name="chg_dt", nullable = true)
    private Date chg_dt;

    @Column(name="chg_id",length = 25,nullable = true)
    private String chg_id;

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public UserAuthTblId getId() { return this.id; }

    @Override
    public boolean isNew() { return isNew || this.id == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }

    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserTbl user;

}
