package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserTbl is a Querydsl query type for UserTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserTbl extends EntityPathBase<UserTbl> {

    private static final long serialVersionUID = -1018147736L;

    public static final QUserTbl userTbl = new QUserTbl("userTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final StringPath userBirth = createString("userBirth");

    public final StringPath userId = createString("userId");

    public final StringPath userIdHash = createString("userIdHash");

    public final StringPath userNm = createString("userNm");

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public final StringPath userPw = createString("userPw");

    public final StringPath userPwSalt = createString("userPwSalt");

    public QUserTbl(String variable) {
        super(UserTbl.class, forVariable(variable));
    }

    public QUserTbl(Path<? extends UserTbl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserTbl(PathMetadata metadata) {
        super(UserTbl.class, metadata);
    }

}

