package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserLocationDetailTblId is a Querydsl query type for UserLocationDetailTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserLocationDetailTblId extends BeanPath<UserLocationDetailTblId> {

    private static final long serialVersionUID = -2084970147L;

    public static final QUserLocationDetailTblId userLocationDetailTblId = new QUserLocationDetailTblId("userLocationDetailTblId");

    public final StringPath addrType = createString("addrType");

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserLocationDetailTblId(String variable) {
        super(UserLocationDetailTblId.class, forVariable(variable));
    }

    public QUserLocationDetailTblId(Path<? extends UserLocationDetailTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserLocationDetailTblId(PathMetadata metadata) {
        super(UserLocationDetailTblId.class, metadata);
    }

}

