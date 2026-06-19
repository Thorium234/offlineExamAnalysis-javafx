package com.thorium.application.dto;

public record ClassStreamDto(
        Long id,
        String code,
        int form,
        String stream,
        String displayName
) {
}
