package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserLocationDetailTbl is a Querydsl query type for UserLocationDetailTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLocationDetailTbl extends EntityPathBase<UserLocationDetailTbl> {

    private static final long serialVersionUID = 2067101858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserLocationDetailTbl userLocationDetailTbl = new QUserLocationDetailTbl("userLocationDetailTbl");

    public final StringPath addr = createString("addr");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final QUserLocationDetailTblId id;

    public final NumberPath<java.math.BigDecimal> latY = createNumber("latY", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lngX = createNumber("lngX", java.math.BigDecimal.class);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final QUserTbl user;

    public QUserLocationDetailTbl(String variable) {
        this(UserLocationDetailTbl.class, forVariable(variable), INITS);
    }

    public QUserLocationDetailTbl(Path<? extends UserLocationDetailTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserLocationDetailTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserLocationDetailTbl(PathMetadata metadata, PathInits inits) {
        this(UserLocationDetailTbl.class, metadata, inits);
    }

    public QUserLocationDetailTbl(Class<? extends UserLocationDetailTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QUserLocationDetailTblId(forProperty("id")) : null;
        this.user = inits.isInitialized("user") ? new QUserTbl(forProperty("user")) : null;
    }

}

