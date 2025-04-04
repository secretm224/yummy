package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTypeSub is a Querydsl query type for StoreTypeSub
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTypeSub extends EntityPathBase<StoreTypeSub> {

    private static final long serialVersionUID = 1589394736L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreTypeSub storeTypeSub = new QStoreTypeSub("storeTypeSub");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final NumberPath<Long> majorType = createNumber("majorType", Long.class);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final QStoreTypeMajor storeTypeMajor;

    public final NumberPath<Long> subType = createNumber("subType", Long.class);

    public final StringPath typeName = createString("typeName");

    public QStoreTypeSub(String variable) {
        this(StoreTypeSub.class, forVariable(variable), INITS);
    }

    public QStoreTypeSub(Path<? extends StoreTypeSub> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreTypeSub(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreTypeSub(PathMetadata metadata, PathInits inits) {
        this(StoreTypeSub.class, metadata, inits);
    }

    public QStoreTypeSub(Class<? extends StoreTypeSub> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.storeTypeMajor = inits.isInitialized("storeTypeMajor") ? new QStoreTypeMajor(forProperty("storeTypeMajor")) : null;
    }

}

