package com.example.pedalboard.domain.cable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LengthTest {

	@Test
	void shouldThrowExceptionForZero() {
		assertThrows(IllegalArgumentException.class, () -> new Length(0.0));
	}

	@ParameterizedTest
	@ValueSource(doubles = {-1.0, -0.1, -100.0})
	void shouldThrowExceptionForNegativeValues(double negativeValue) {
		assertThrows(IllegalArgumentException.class, () -> new Length(negativeValue));
	}

	@ParameterizedTest
	@ValueSource(doubles = {1.0, 0.1, 100.0, 123.456})
	void shouldAcceptPositiveValues(double lengthValue) {
		Length length = new Length(lengthValue);
		assertEquals(lengthValue, length.value());
	}

	@Test
	void shouldAcceptVerySmallPositiveValues() {
		Length length = new Length(0.0001);

		assertEquals(0.0001, length.value());
	}

	@Test
	void shouldAcceptVeryLargePositiveValues() {
		Length length = new Length(1000000.0);

		assertEquals(1000000.0, length.value());
	}

	@Test
	void equals_shouldReturnTrueForSameValue() {
		Length length1 = new Length(10.5);
		Length length2 = new Length(10.5);

		assertEquals(length1, length2);
	}

	@Test
	void equals_shouldReturnFalseForDifferentValues() {
		Length length1 = new Length(10.5);
		Length length2 = new Length(20.5);

		assertNotEquals(length1, length2);
	}

	@Test
	void equals_shouldHandleDoublePrecision() {
		Length length1 = new Length(10.0);
		Length length2 = new Length(10.0000000001);

		// These should be different due to double comparison
		assertNotEquals(length1, length2);
	}

	@Test
	void hashCode_shouldBeEqualForSameValue() {
		Length length1 = new Length(10.5);
		Length length2 = new Length(10.5);

		assertEquals(length1.hashCode(), length2.hashCode());
	}

	@Test
	void toString_shouldReturnFormattedString() {
		Length length = new Length(10.5);
		String result = length.toString();

		assertEquals("Length{value=10.5}", result);
	}
}
