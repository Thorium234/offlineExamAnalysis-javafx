package com.thorium.application.dto;

public record PlacementValidationDto(
        boolean valid,
        String reason
) {
    public static PlacementValidationDto ok() {
        return new PlacementValidationDto(true, null);
    }

    public static PlacementValidationDto invalid(String reason) {
        return new PlacementValidationDto(false, reason);
    }
}
