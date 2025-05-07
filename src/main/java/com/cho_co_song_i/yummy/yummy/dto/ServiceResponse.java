package com.cho_co_song_i.yummy.yummy.dto;

import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse<T> {
    private PublicStatus publicStatus;
    private T data;

    public static<T> ServiceResponse<T> of(PublicStatus status, T data) {
        return new ServiceResponse<>(status, data);
    }

    public static<T> ServiceResponse<T> empty(PublicStatus status) {
        return new ServiceResponse<>(status, null);
    }
}