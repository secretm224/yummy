package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserAuthTblId is a Querydsl query type for UserAuthTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserAuthTblId extends BeanPath<UserAuthTblId> {

    private static final long serialVersionUID = -259444261L;

    public static final QUserAuthTblId userAuthTblId = new QUserAuthTblId("userAuthTblId");

    public final NumberPath<Long> loginChannel = createNumber("loginChannel", Long.class);

    public final NumberPath<Long> tokenId = createNumber("tokenId", Long.class);

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserAuthTblId(String variable) {
        super(UserAuthTblId.class, forVariable(variable));
    }

    public QUserAuthTblId(Path<? extends UserAuthTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserAuthTblId(PathMetadata metadata) {
        super(UserAuthTblId.class, metadata);
    }

}

