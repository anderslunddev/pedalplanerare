package io.github.anderslunddev.pedalboard.domain.pedal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlacementTest {

	@Test
	void shouldThrowExceptionForZero() {
		assertThrows(IllegalArgumentException.class, () -> new Placement(0));
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -10})
	void shouldThrowExceptionForNegativeValues(int negativeValue) {
		assertThrows(IllegalArgumentException.class, () -> new Placement(negativeValue));
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 100})
	void shouldAcceptPositiveValues(int placementValue) {
		Placement placement = new Placement(placementValue);
		assertEquals(placementValue, placement.value());
	}

	@Test
	void shouldAcceptMinimumValidValue() {
		Placement placement = new Placement(1);

		assertEquals(1, placement.value());
	}

	@Test
	void shouldAcceptLargePositiveValues() {
		Placement placement = new Placement(Integer.MAX_VALUE);

		assertEquals(Integer.MAX_VALUE, placement.value());
	}

	@Test
	void equals_shouldReturnTrueForSameValue() {
		Placement placement1 = new Placement(5);
		Placement placement2 = new Placement(5);

		assertEquals(placement1, placement2);
	}

	@Test
	void equals_shouldReturnFalseForDifferentValues() {
		Placement placement1 = new Placement(5);
		Placement placement2 = new Placement(10);

		assertNotEquals(placement1, placement2);
	}

	@Test
	void hashCode_shouldBeEqualForSameValue() {
		Placement placement1 = new Placement(5);
		Placement placement2 = new Placement(5);

		assertEquals(placement1.hashCode(), placement2.hashCode());
	}

	@Test
	void toString_shouldReturnFormattedString() {
		Placement placement = new Placement(5);
		String result = placement.toString();

		assertEquals("Placement{value=5}", result);
	}

	@Test
	void value_shouldReturnCorrectValue() {
		Placement placement = new Placement(42);

		assertEquals(42, placement.value());
	}
}
