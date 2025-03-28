package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreLocationInfoTbl is a Querydsl query type for StoreLocationInfoTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreLocationInfoTbl extends EntityPathBase<StoreLocationInfoTbl> {

    private static final long serialVersionUID = -528306875L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreLocationInfoTbl storeLocationInfoTbl = new QStoreLocationInfoTbl("storeLocationInfoTbl");

    public final StringPath address = createString("address");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final NumberPath<java.math.BigDecimal> lat = createNumber("lat", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lng = createNumber("lng", java.math.BigDecimal.class);

    public final StringPath locationCity = createString("locationCity");

    public final StringPath locationCounty = createString("locationCounty");

    public final StringPath locationDistrict = createString("locationDistrict");

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final NumberPath<Long> seq = createNumber("seq", Long.class);

    public final QStore store;

    public QStoreLocationInfoTbl(String variable) {
        this(StoreLocationInfoTbl.class, forVariable(variable), INITS);
    }

    public QStoreLocationInfoTbl(Path<? extends StoreLocationInfoTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreLocationInfoTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreLocationInfoTbl(PathMetadata metadata, PathInits inits) {
        this(StoreLocationInfoTbl.class, metadata, inits);
    }

    public QStoreLocationInfoTbl(Class<? extends StoreLocationInfoTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

