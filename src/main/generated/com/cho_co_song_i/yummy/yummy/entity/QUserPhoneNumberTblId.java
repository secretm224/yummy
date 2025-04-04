package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserPhoneNumberTblId is a Querydsl query type for UserPhoneNumberTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserPhoneNumberTblId extends BeanPath<UserPhoneNumberTblId> {

    private static final long serialVersionUID = 121625848L;

    public static final QUserPhoneNumberTblId userPhoneNumberTblId = new QUserPhoneNumberTblId("userPhoneNumberTblId");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QUserPhoneNumberTblId(String variable) {
        super(UserPhoneNumberTblId.class, forVariable(variable));
    }

    public QUserPhoneNumberTblId(Path<? extends UserPhoneNumberTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserPhoneNumberTblId(PathMetadata metadata) {
        super(UserPhoneNumberTblId.class, metadata);
    }

}

