package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStore is a Querydsl query type for Store
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStore extends EntityPathBase<Store> {

    private static final long serialVersionUID = -874374506L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStore store = new QStore("store");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final StringPath name = createString("name");

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final NumberPath<Long> seq = createNumber("seq", Long.class);

    public final QStoreLocationInfoTbl storeLocations;

    public final ListPath<StoreTypeLinkTbl, QStoreTypeLinkTbl> storeTypeLinks = this.<StoreTypeLinkTbl, QStoreTypeLinkTbl>createList("storeTypeLinks", StoreTypeLinkTbl.class, QStoreTypeLinkTbl.class, PathInits.DIRECT2);

    public final StringPath type = createString("type");

    public final ComparablePath<Character> useYn = createComparable("useYn", Character.class);

    public final QZeroPossibleMarket zeroPossibles;

    public QStore(String variable) {
        this(Store.class, forVariable(variable), INITS);
    }

    public QStore(Path<? extends Store> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStore(PathMetadata metadata, PathInits inits) {
        this(Store.class, metadata, inits);
    }

    public QStore(Class<? extends Store> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.storeLocations = inits.isInitialized("storeLocations") ? new QStoreLocationInfoTbl(forProperty("storeLocations"), inits.get("storeLocations")) : null;
        this.zeroPossibles = inits.isInitialized("zeroPossibles") ? new QZeroPossibleMarket(forProperty("zeroPossibles"), inits.get("zeroPossibles")) : null;
    }

}

