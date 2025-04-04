package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserPhoneNumberTbl is a Querydsl query type for UserPhoneNumberTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPhoneNumberTbl extends EntityPathBase<UserPhoneNumberTbl> {

    private static final long serialVersionUID = 1135320829L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserPhoneNumberTbl userPhoneNumberTbl = new QUserPhoneNumberTbl("userPhoneNumberTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final QUserPhoneNumberTblId id;

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final QUserTbl user;

    public QUserPhoneNumberTbl(String variable) {
        this(UserPhoneNumberTbl.class, forVariable(variable), INITS);
    }

    public QUserPhoneNumberTbl(Path<? extends UserPhoneNumberTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserPhoneNumberTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserPhoneNumberTbl(PathMetadata metadata, PathInits inits) {
        this(UserPhoneNumberTbl.class, metadata, inits);
    }

    public QUserPhoneNumberTbl(Class<? extends UserPhoneNumberTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QUserPhoneNumberTblId(forProperty("id")) : null;
        this.user = inits.isInitialized("user") ? new QUserTbl(forProperty("user")) : null;
    }

}

