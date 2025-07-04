package com.cho_co_song_i.yummy.yummy.entity;

import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "user_tbl")
@NoArgsConstructor
@Getter
public class UserTbl implements Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_pw", nullable = false, columnDefinition = "CHAR(44)")
    private String userPw;

    @Column(name = "user_pw_salt", nullable = false, columnDefinition = "CHAR(24)")
    private String userPwSalt;

    @Column(name = "user_nm", nullable = false, length = 100)
    private String userNm;

    @Column(name = "user_birth", nullable = false, length = 8)
    private String userBirth;

    @Column(name = "user_gender", nullable = true, columnDefinition = "CHAR(1)")
    private String userGender;

    @Column(name = "main_oauth_channel", nullable = true, length = 25)
    private String mainOauthChannel;

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

    @Transient
    private boolean isNew = true;

    public Long getId(){
        return this.userNo;
    }

    public boolean isNew(){
        return this.userNo == null || isNew;
    }

    public void markNotNew(){
        this.isNew = false;
    }

    /**
     * User의 비밀번호와 비밀번호 솔트값 수정 해주는 함수
     * @param hashedPw
     * @param pwSalt
     */
    public void modifyUserPwAndSalt(String hashedPw, String pwSalt) {
        this.userPw = hashedPw;
        this.userPwSalt = pwSalt;
    }

    public UserTbl(JoinMemberDto joinMemberDto, String userPwHash, String saltValue, String regId) {
        Instant nowInstant = Instant.now();
        this.userId = joinMemberDto.getUserId();
        this.userPw = userPwHash;
        this.userPwSalt = saltValue;
        this.userNm = joinMemberDto.getName();
        this.userBirth = joinMemberDto.getBirthDate();
        this.userGender = joinMemberDto.getGender();
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
    }

    /**
     * user의 Oauth 채널을 수정해주는 함수
     * @param mainOauthChannel
     * @param chgId
     */
    public void modifyUserOauthChannel(String mainOauthChannel, String chgId) {
        Instant nowInstant = Instant.now();
        this.mainOauthChannel = mainOauthChannel;
        this.chgDt = Date.from(nowInstant);
        this.chgId = chgId;
    }



}