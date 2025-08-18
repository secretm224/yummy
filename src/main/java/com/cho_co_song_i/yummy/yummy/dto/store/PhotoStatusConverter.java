package com.cho_co_song_i.yummy.yummy.dto.store;

import com.cho_co_song_i.yummy.yummy.enums.PhotoStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PhotoStatusConverter implements AttributeConverter<PhotoStatus, Integer> {
    @Override public Integer convertToDatabaseColumn(PhotoStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    @Override public PhotoStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : PhotoStatus.fromCode(dbData);
    }
}
