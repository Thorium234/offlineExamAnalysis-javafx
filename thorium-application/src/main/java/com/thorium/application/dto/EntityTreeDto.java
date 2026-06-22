package com.thorium.application.dto;

import java.util.List;

public record EntityTreeDto(
        List<EntityTreeNodeDto> teachers,
        List<EntityTreeNodeDto> subjects,
        List<EntityTreeNodeDto> classes,
        List<EntityTreeNodeDto> rooms
) {
}
