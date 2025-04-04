package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEmailTbl is a Querydsl query type for UserEmailTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEmailTbl extends EntityPathBase<UserEmailTbl> {

    private static final long serialVersionUID = 1398723608L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserEmailTbl userEmailTbl = new QUserEmailTbl("userEmailTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final QUserTbl user;

    public final StringPath userEmailAddress = createString("userEmailAddress");

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserEmailTbl(String variable) {
        this(UserEmailTbl.class, forVariable(variable), INITS);
    }

    public QUserEmailTbl(Path<? extends UserEmailTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserEmailTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserEmailTbl(PathMetadata metadata, PathInits inits) {
        this(UserEmailTbl.class, metadata, inits);
    }

    public QUserEmailTbl(Class<? extends UserEmailTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserTbl(forProperty("user")) : null;
    }

}

