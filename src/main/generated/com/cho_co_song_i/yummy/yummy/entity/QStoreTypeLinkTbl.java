package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTypeLinkTbl is a Querydsl query type for StoreTypeLinkTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTypeLinkTbl extends EntityPathBase<StoreTypeLinkTbl> {

    private static final long serialVersionUID = 2031051860L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreTypeLinkTbl storeTypeLinkTbl = new QStoreTypeLinkTbl("storeTypeLinkTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final QStoreTypeLinkTblId id;

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final QStore store;

    public QStoreTypeLinkTbl(String variable) {
        this(StoreTypeLinkTbl.class, forVariable(variable), INITS);
    }

    public QStoreTypeLinkTbl(Path<? extends StoreTypeLinkTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreTypeLinkTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreTypeLinkTbl(PathMetadata metadata, PathInits inits) {
        this(StoreTypeLinkTbl.class, metadata, inits);
    }

    public QStoreTypeLinkTbl(Class<? extends StoreTypeLinkTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QStoreTypeLinkTblId(forProperty("id")) : null;
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

