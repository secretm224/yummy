package com.cho_co_song_i.yummy.yummy.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLocationCityTblId is a Querydsl query type for LocationCityTblId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QLocationCityTblId extends BeanPath<LocationCityTblId> {

    private static final long serialVersionUID = 586221774L;

    public static final QLocationCityTblId locationCityTblId = new QLocationCityTblId("locationCityTblId");

    public final NumberPath<Long> locationCityCode = createNumber("locationCityCode", Long.class);

    public final NumberPath<Long> locationCountyCode = createNumber("locationCountyCode", Long.class);

    public QLocationCityTblId(String variable) {
        super(LocationCityTblId.class, forVariable(variable));
    }

    public QLocationCityTblId(Path<? extends LocationCityTblId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLocationCityTblId(PathMetadata metadata) {
        super(LocationCityTblId.class, metadata);
    }

}

