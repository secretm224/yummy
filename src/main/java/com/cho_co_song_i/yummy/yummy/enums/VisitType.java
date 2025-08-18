package com.cho_co_song_i.yummy.yummy.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum VisitType {
    DINE_IN(0), TAKEOUT(1), DELIVERY(2);
    private final int code;
    VisitType(int code) { this.code = code; }
    public int getCode() { return code; }
    public static VisitType fromCode(int code) {
        for (var v : values()) if (v.code == code) return v;
        throw new IllegalArgumentException("Unknown VisitType code: " + code);
    }
}


