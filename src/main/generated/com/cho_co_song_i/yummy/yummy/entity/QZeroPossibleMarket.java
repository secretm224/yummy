package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QZeroPossibleMarket is a Querydsl query type for ZeroPossibleMarket
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QZeroPossibleMarket extends EntityPathBase<ZeroPossibleMarket> {

    private static final long serialVersionUID = -1724358080L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QZeroPossibleMarket zeroPossibleMarket = new QZeroPossibleMarket("zeroPossibleMarket");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final StringPath name = createString("name");

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final NumberPath<Long> seq = createNumber("seq", Long.class);

    public final QStore store;

    public final ComparablePath<Character> useYn = createComparable("useYn", Character.class);

    public QZeroPossibleMarket(String variable) {
        this(ZeroPossibleMarket.class, forVariable(variable), INITS);
    }

    public QZeroPossibleMarket(Path<? extends ZeroPossibleMarket> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QZeroPossibleMarket(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QZeroPossibleMarket(PathMetadata metadata, PathInits inits) {
        this(ZeroPossibleMarket.class, metadata, inits);
    }

    public QZeroPossibleMarket(Class<? extends ZeroPossibleMarket> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

