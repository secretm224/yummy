package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreRecommendTblId is a Querydsl query type for StoreRecommendTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QStoreRecommendTblId extends BeanPath<StoreRecommendTblId> {

    private static final long serialVersionUID = -839610797L;

    public static final QStoreRecommendTblId storeRecommendTblId = new QStoreRecommendTblId("storeRecommendTblId");

    public final NumberPath<Long> recommendSeq = createNumber("recommendSeq", Long.class);

    public final NumberPath<Long> seq = createNumber("seq", Long.class);

    public QStoreRecommendTblId(String variable) {
        super(StoreRecommendTblId.class, forVariable(variable));
    }

    public QStoreRecommendTblId(Path<? extends StoreRecommendTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreRecommendTblId(PathMetadata metadata) {
        super(StoreRecommendTblId.class, metadata);
    }

}

