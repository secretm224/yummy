package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLocationDistrictTblId is a Querydsl query type for LocationDistrictTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QLocationDistrictTblId extends BeanPath<LocationDistrictTblId> {

    private static final long serialVersionUID = -160084853L;

    public static final QLocationDistrictTblId locationDistrictTblId = new QLocationDistrictTblId("locationDistrictTblId");

    public final NumberPath<Long> locationCityCode = createNumber("locationCityCode", Long.class);

    public final NumberPath<Long> locationCountyCode = createNumber("locationCountyCode", Long.class);

    public final NumberPath<Long> locationDistrictCode = createNumber("locationDistrictCode", Long.class);

    public QLocationDistrictTblId(String variable) {
        super(LocationDistrictTblId.class, forVariable(variable));
    }

    public QLocationDistrictTblId(Path<? extends LocationDistrictTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLocationDistrictTblId(PathMetadata metadata) {
        super(LocationDistrictTblId.class, metadata);
    }

}

