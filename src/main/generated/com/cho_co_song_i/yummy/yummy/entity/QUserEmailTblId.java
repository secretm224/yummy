package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserEmailTblId is a Querydsl query type for UserEmailTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserEmailTblId extends BeanPath<UserEmailTblId> {

    private static final long serialVersionUID = -151373997L;

    public static final QUserEmailTblId userEmailTblId = new QUserEmailTblId("userEmailTblId");

    public final StringPath userEmailAddress = createString("userEmailAddress");

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserEmailTblId(String variable) {
        super(UserEmailTblId.class, forVariable(variable));
    }

    public QUserEmailTblId(Path<? extends UserEmailTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEmailTblId(PathMetadata metadata) {
        super(UserEmailTblId.class, metadata);
    }

}

