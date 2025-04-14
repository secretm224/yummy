package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAuthTbl is a Querydsl query type for UserAuthTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAuthTbl extends EntityPathBase<UserAuthTbl> {

    private static final long serialVersionUID = 66769056L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAuthTbl userAuthTbl = new QUserAuthTbl("userAuthTbl");

    public final NumberPath<Long> authNo = createNumber("authNo", Long.class);

    public final DateTimePath<java.util.Date> chg_dt = createDateTime("chg_dt", java.util.Date.class);

    public final StringPath chg_id = createString("chg_id");

    public final StringPath login_channel = createString("login_channel");

    public final DateTimePath<java.util.Date> reg_dt = createDateTime("reg_dt", java.util.Date.class);

    public final StringPath reg_id = createString("reg_id");

    public final StringPath token_id = createString("token_id");

    public final QUserTbl user;

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserAuthTbl(String variable) {
        this(UserAuthTbl.class, forVariable(variable), INITS);
    }

    public QUserAuthTbl(Path<? extends UserAuthTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAuthTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAuthTbl(PathMetadata metadata, PathInits inits) {
        this(UserAuthTbl.class, metadata, inits);
    }

    public QUserAuthTbl(Class<? extends UserAuthTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserTbl(forProperty("user")) : null;
    }

}

