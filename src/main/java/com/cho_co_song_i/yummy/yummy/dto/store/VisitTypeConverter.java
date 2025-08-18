package com.cho_co_song_i.yummy.yummy.dto.store;

import com.cho_co_song_i.yummy.yummy.enums.VisitType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VisitTypeConverter implements AttributeConverter<VisitType, Integer> {
    @Override public Integer convertToDatabaseColumn(VisitType attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    @Override public VisitType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : VisitType.fromCode(dbData);
    }
}