package io.github.anderslunddev.pedalboard.domain.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColorTest {

	@Test
	void shouldThrowExceptionForNullColor() {
		assertThrows(NullPointerException.class, () -> new Color(null));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void shouldThrowExceptionForBlankColor(String blankColor) {
		assertThrows(IllegalArgumentException.class, () -> new Color(blankColor));
	}

	@ParameterizedTest
	@ValueSource(strings = {"red", "#GGGGGG", "FFFFFF", "#FF", "#FFFF", "#FFFFFFF", "#"})
	void shouldThrowExceptionForInvalidHexFormats(String invalidColor) {
		assertThrows(IllegalArgumentException.class, () -> new Color(invalidColor));
	}

	@ParameterizedTest
	@ValueSource(strings = {"#000000", "#FFFFFF", "#ff0000", "#00FF00", "#0000ff", "#123456"})
	void shouldAcceptValidHexColor6Digit(String colorValue) {
		Color color = new Color(colorValue);
		assertEquals(colorValue, color.value());
	}

	@ParameterizedTest
	@ValueSource(strings = {"#000", "#FFF", "#f00", "#0F0", "#00f", "#abc"})
	void shouldAcceptValidHexColor3Digit(String colorValue) {
		Color color = new Color(colorValue);
		assertEquals(colorValue, color.value());
	}

	@ParameterizedTest
	@ValueSource(strings = {"#AbCdEf", "#aBc"})
	void shouldAcceptMixedCaseHexColors(String colorValue) {
		Color color = new Color(colorValue);
		assertEquals(colorValue, color.value());
	}

	@Test
	void equals_shouldReturnTrueForSameValue() {
		Color color1 = new Color("#ff0000");
		Color color2 = new Color("#ff0000");

		assertEquals(color1, color2);
	}

	@Test
	void equals_shouldReturnFalseForDifferentValues() {
		Color color1 = new Color("#ff0000");
		Color color2 = new Color("#00ff00");

		assertNotEquals(color1, color2);
	}

	@Test
	void equals_shouldBeCaseSensitive() {
		Color color1 = new Color("#ff0000");
		Color color2 = new Color("#FF0000");

		// These are NOT equal because Color stores the exact string value
		assertNotEquals(color1, color2);
	}

	@Test
	void hashCode_shouldBeEqualForSameValue() {
		Color color1 = new Color("#ff0000");
		Color color2 = new Color("#ff0000");

		assertEquals(color1.hashCode(), color2.hashCode());
	}

	@Test
	void toString_shouldReturnFormattedString() {
		Color color = new Color("#ff0000");
		String result = color.toString();

		assertEquals("Color{value='#ff0000'}", result);
	}
}
