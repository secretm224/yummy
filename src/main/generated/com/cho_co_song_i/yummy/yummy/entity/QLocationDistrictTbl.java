package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLocationDistrictTbl is a Querydsl query type for LocationDistrictTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLocationDistrictTbl extends EntityPathBase<LocationDistrictTbl> {

    private static final long serialVersionUID = 1653462864L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLocationDistrictTbl locationDistrictTbl = new QLocationDistrictTbl("locationDistrictTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final QLocationDistrictTblId id;

    public final QLocationCityTbl locationCity;

    public final StringPath locationDistrict = createString("locationDistrict");

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public QLocationDistrictTbl(String variable) {
        this(LocationDistrictTbl.class, forVariable(variable), INITS);
    }

    public QLocationDistrictTbl(Path<? extends LocationDistrictTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLocationDistrictTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLocationDistrictTbl(PathMetadata metadata, PathInits inits) {
        this(LocationDistrictTbl.class, metadata, inits);
    }

    public QLocationDistrictTbl(Class<? extends LocationDistrictTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QLocationDistrictTblId(forProperty("id")) : null;
        this.locationCity = inits.isInitialized("locationCity") ? new QLocationCityTbl(forProperty("locationCity"), inits.get("locationCity")) : null;
    }

}

