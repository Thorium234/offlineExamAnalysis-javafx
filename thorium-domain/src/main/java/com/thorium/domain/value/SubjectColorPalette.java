package com.thorium.domain.value;

public final class SubjectColorPalette {

    private static final String[] PASTEL_COLORS = {
            "#BFDBFE", "#BBF7D0", "#FEF08A", "#FBCFE8", "#DDD6FE",
            "#A5F3FC", "#FED7AA", "#D9F99D", "#FECDD3", "#E9D5FF",
            "#C7D2FE", "#99F6E4", "#FDE68A", "#FCA5A5", "#D8B4FE"
    };

    private SubjectColorPalette() {
    }

    public static String colorForSubject(long subjectId) {
        return PASTEL_COLORS[(int) (Math.abs(subjectId) % PASTEL_COLORS.length)];
    }

    public static String resolveColor(Long subjectId, String storedColor) {
        if (storedColor != null && !storedColor.isBlank()) {
            return storedColor;
        }
        if (subjectId != null) {
            return colorForSubject(subjectId);
        }
        return "#E2E8F0";
    }
}
