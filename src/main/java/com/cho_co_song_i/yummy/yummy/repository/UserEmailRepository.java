package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserEmailTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserEmailRepository extends JpaRepository<UserEmailTbl, Long> {

    @Query(value = "SELECT EXISTS (SELECT 1 FROM user_email_tbl WHERE user_email_address = :email)", nativeQuery = true)
    Long existsByEmail(@Param("email") String email);


}
