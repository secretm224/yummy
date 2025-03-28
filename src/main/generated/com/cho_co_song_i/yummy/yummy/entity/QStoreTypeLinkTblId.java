package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreTypeLinkTblId is a Querydsl query type for StoreTypeLinkTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QStoreTypeLinkTblId extends BeanPath<StoreTypeLinkTblId> {

    private static final long serialVersionUID = 1925687439L;

    public static final QStoreTypeLinkTblId storeTypeLinkTblId = new QStoreTypeLinkTblId("storeTypeLinkTblId");

    public final NumberPath<Long> seq = createNumber("seq", Long.class);

    public final NumberPath<Integer> subType = createNumber("subType", Integer.class);

    public QStoreTypeLinkTblId(String variable) {
        super(StoreTypeLinkTblId.class, forVariable(variable));
    }

    public QStoreTypeLinkTblId(Path<? extends StoreTypeLinkTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreTypeLinkTblId(PathMetadata metadata) {
        super(StoreTypeLinkTblId.class, metadata);
    }

}

