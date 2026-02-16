package io.github.anderslunddev.pedalboard.domain.value;

import java.util.Objects;
import java.util.regex.Pattern;

public record Color(String value) {

	// Hex color patterns: #RRGGBB or #RGB
	private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$");

	public Color {
		Objects.requireNonNull(value, "Color must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Color must not be blank");
		}
		if (!isValidHexColor(value)) {
			throw new IllegalArgumentException(
					"Color must be a valid hex color format (#RRGGBB or #RGB), got: " + value);
		}
	}

	private static boolean isValidHexColor(String color) {
		return HEX_COLOR_PATTERN.matcher(color).matches();
	}

	@Override
	public String toString() {
		return "Color{value='" + value + "'}";
	}
}
