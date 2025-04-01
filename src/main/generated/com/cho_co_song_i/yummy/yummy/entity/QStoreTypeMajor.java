package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTypeMajor is a Querydsl query type for StoreTypeMajor
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTypeMajor extends EntityPathBase<StoreTypeMajor> {

    private static final long serialVersionUID = -1606141783L;

    public static final QStoreTypeMajor storeTypeMajor = new QStoreTypeMajor("storeTypeMajor");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final NumberPath<Long> majorType = createNumber("majorType", Long.class);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public final ListPath<StoreTypeSub, QStoreTypeSub> storeTypeSubs = this.<StoreTypeSub, QStoreTypeSub>createList("storeTypeSubs", StoreTypeSub.class, QStoreTypeSub.class, PathInits.DIRECT2);

    public final StringPath typeName = createString("typeName");

    public QStoreTypeMajor(String variable) {
        super(StoreTypeMajor.class, forVariable(variable));
    }

    public QStoreTypeMajor(Path<? extends StoreTypeMajor> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreTypeMajor(PathMetadata metadata) {
        super(StoreTypeMajor.class, metadata);
    }

}

