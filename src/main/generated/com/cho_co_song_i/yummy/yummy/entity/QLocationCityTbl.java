package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLocationCityTbl is a Querydsl query type for LocationCityTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLocationCityTbl extends EntityPathBase<LocationCityTbl> {

    private static final long serialVersionUID = 742508627L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLocationCityTbl locationCityTbl = new QLocationCityTbl("locationCityTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final QLocationCityTblId id;

    public final StringPath locationCity = createString("locationCity");

    public final QLocationCountyTbl locationCounty;

    public final ListPath<LocationDistrictTbl, QLocationDistrictTbl> locationDistricts = this.<LocationDistrictTbl, QLocationDistrictTbl>createList("locationDistricts", LocationDistrictTbl.class, QLocationDistrictTbl.class, PathInits.DIRECT2);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public QLocationCityTbl(String variable) {
        this(LocationCityTbl.class, forVariable(variable), INITS);
    }

    public QLocationCityTbl(Path<? extends LocationCityTbl> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLocationCityTbl(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLocationCityTbl(PathMetadata metadata, PathInits inits) {
        this(LocationCityTbl.class, metadata, inits);
    }

    public QLocationCityTbl(Class<? extends LocationCityTbl> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QLocationCityTblId(forProperty("id")) : null;
        this.locationCounty = inits.isInitialized("locationCounty") ? new QLocationCountyTbl(forProperty("locationCounty")) : null;
    }

}

