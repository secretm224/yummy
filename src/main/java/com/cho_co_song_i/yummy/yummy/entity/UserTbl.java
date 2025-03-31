package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTbl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long user_no;

    @Column(name = "user_nm", nullable = false, length = 100)
    private String user_nm;

    @Column(name = "reg_dt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reg_dt;

    @Column(name = "reg_id", nullable = false, length = 25)
    private String reg_id;

    @Column(name = "chg_dt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date chg_dt;

    @Column(name = "chg_id", nullable = false, length = 25)
    private String chg_id;
}




//@Entity('user_tbl')
//export class User {
//    @PrimaryGeneratedColumn()
//    user_no: number;
//
//    @Column({ type: 'varchar', length: 100, nullable: false })
//user_nm: string;
//
//@Column({ type: 'timestamp', nullable: true })
//reg_dt: Date;
//
//@Column({ type: 'varchar', length: 25, nullable: true })
//reg_id: string;
//
//@Column({ type: 'timestamp', nullable: true })
//chg_dt: Date;
//
//@Column({ type: 'varchar', length: 25, nullable: true })
//chg_id: string;
//
//@OneToMany(() => UserAuth, (userAuth) => userAuth.user)
//auths: UserAuth[];
//
//@OneToMany(() => UserDetail, (userDetail) => userDetail.user)
//details: UserDetail[];
//}