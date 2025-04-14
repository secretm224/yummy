package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserDetailTbl is a Querydsl query type for UserDetailTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserDetailTbl extends EntityPathBase<UserDetailTbl> {

    private static final long serialVersionUID = 612230871L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserDetailTbl userDetailTbl = new QUserDetailTbl("userDetailTbl");

    public final StringPath addr = createString("addr");

    public final StringPath addrType = createString("addrType");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final NumberPath<Long> detailNo = createNumber("detailNo", Long.class);

    public final NumberPath<java.math.BigDecimal> latY = createNumber("latY", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lngX = createNumber("lngX", java.math.BigDecimal.class);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final QUserTbl user;

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserDetailTbl(String variable) {
        this(UserDetailTbl.class, forVariable(variable), INITS);
    }

    public QUserDetailTbl(Path<? extends UserDetailTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserDetailTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserDetailTbl(PathMetadata metadata, PathInits inits) {
        this(UserDetailTbl.class, metadata, inits);
    }

    public QUserDetailTbl(Class<? extends UserDetailTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserTbl(forProperty("user")) : null;
    }

}

