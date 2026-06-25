package com.thorium.application.dto;

public record TeacherDto(
        Long id,
        String code,
        String name,
        boolean active
) {
}
