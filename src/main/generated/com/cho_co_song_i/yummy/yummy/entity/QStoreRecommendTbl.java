package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreRecommendTbl is a Querydsl query type for StoreRecommendTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreRecommendTbl extends EntityPathBase<StoreRecommendTbl> {

    private static final long serialVersionUID = -2096960744L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreRecommendTbl storeRecommendTbl = new QStoreRecommendTbl("storeRecommendTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final QStoreRecommendTblId id;

    public final DateTimePath<java.util.Date> recommendEndDt = createDateTime("recommendEndDt", java.util.Date.class);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public QStoreRecommendTbl(String variable) {
        this(StoreRecommendTbl.class, forVariable(variable), INITS);
    }

    public QStoreRecommendTbl(Path<? extends StoreRecommendTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreRecommendTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreRecommendTbl(PathMetadata metadata, PathInits inits) {
        this(StoreRecommendTbl.class, metadata, inits);
    }

    public QStoreRecommendTbl(Class<? extends StoreRecommendTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QStoreRecommendTblId(forProperty("id")) : null;
    }

}

