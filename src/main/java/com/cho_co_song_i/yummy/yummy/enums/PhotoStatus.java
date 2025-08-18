package com.cho_co_song_i.yummy.yummy.enums;

public enum PhotoStatus {
    PUBLISHED(0), BLINDED(1), DELETED(2);
    private final int code;
    PhotoStatus(int code) { this.code = code; }
    public int getCode() { return code; }
    public static PhotoStatus fromCode(int code) {
        for (var v : values()) if (v.code == code) return v;
        throw new IllegalArgumentException("Unknown PhotoStatus code: " + code);
    }
}