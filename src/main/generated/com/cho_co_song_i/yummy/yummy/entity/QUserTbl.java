package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserTbl is a Querydsl query type for UserTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserTbl extends EntityPathBase<UserTbl> {

    private static final long serialVersionUID = -1018147736L;

    public static final QUserTbl userTbl = new QUserTbl("userTbl");

    public final DateTimePath<java.util.Date> chg_dt = createDateTime("chg_dt", java.util.Date.class);

    public final StringPath chg_id = createString("chg_id");

    public final DateTimePath<java.util.Date> reg_dt = createDateTime("reg_dt", java.util.Date.class);

    public final StringPath reg_id = createString("reg_id");

    public final StringPath user_nm = createString("user_nm");

    public final NumberPath<Long> user_no = createNumber("user_no", Long.class);

    public QUserTbl(String variable) {
        super(UserTbl.class, forVariable(variable));
    }

    public QUserTbl(Path<? extends UserTbl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserTbl(PathMetadata metadata) {
        super(UserTbl.class, metadata);
    }

}

