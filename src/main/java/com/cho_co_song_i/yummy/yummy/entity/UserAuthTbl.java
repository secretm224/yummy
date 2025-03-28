package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.util.Date;

@Table(name = "user_auth_tbl")
public class UserAuthTbl implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_no")
    private Long authNo;

    @ManyToOne
    @JoinColumn(name="user_no")
    private UserTbl user;

    @Column(name = "user_no", nullable = false, insertable = false, updatable = false)
    private int userNo;

    @Column(name="login_channel",length = 10,nullable = false)
    private String login_channel;

    @Column(name="token_id",length = 255,nullable = false)
    private String token_id;

    @Column(name="reg_dt",nullable = false)
    private Date reg_dt;

    @Column(name="reg_id",length = 25,nullable = false)
    private String reg_id;

    @Column(name="chg_dt",nullable = false)
    private Date chg_dt;

    @Column(name="chg_id",length = 25,nullable = false)
    private String chg_id;

    @Transient
    private  boolean isNew = true;

    public Long getId() {
        return this.authNo;
    }

    public boolean isNew() {
        return false;
    }

    public void markNotNew(){
        this.isNew = false;
    }
}
