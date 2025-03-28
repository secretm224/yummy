package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLocationCountyTbl is a Querydsl query type for LocationCountyTbl
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLocationCountyTbl extends EntityPathBase<LocationCountyTbl> {

    private static final long serialVersionUID = -1239019500L;

    public static final QLocationCountyTbl locationCountyTbl = new QLocationCountyTbl("locationCountyTbl");

    public final DateTimePath<java.util.Date> chgDt = createDateTime("chgDt", java.util.Date.class);

    public final StringPath chgId = createString("chgId");

    public final ListPath<LocationCityTbl, QLocationCityTbl> locationCities = this.<LocationCityTbl, QLocationCityTbl>createList("locationCities", LocationCityTbl.class, QLocationCityTbl.class, PathInits.DIRECT2);

    public final StringPath locationCounty = createString("locationCounty");

    public final NumberPath<Long> locationCountyCode = createNumber("locationCountyCode", Long.class);

    public final DateTimePath<java.util.Date> regDt = createDateTime("regDt", java.util.Date.class);

    public final StringPath regId = createString("regId");

    public QLocationCountyTbl(String variable) {
        super(LocationCountyTbl.class, forVariable(variable));
    }

    public QLocationCountyTbl(Path<? extends LocationCountyTbl> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLocationCountyTbl(PathMetadata metadata) {
        super(LocationCountyTbl.class, metadata);
    }

}

