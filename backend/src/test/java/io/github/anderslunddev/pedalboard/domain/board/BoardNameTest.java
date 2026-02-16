package io.github.anderslunddev.pedalboard.domain.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardNameTest {

	@Test
	void shouldThrowExceptionForNullName() {
		assertThrows(NullPointerException.class, () -> new BoardName(null));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   ", "\t", "\n"})
	void shouldThrowExceptionForBlankName(String blankName) {
		assertThrows(IllegalArgumentException.class, () -> new BoardName(blankName));
	}

	@ParameterizedTest
	@ValueSource(strings = {"My Board", "Test Board 123", "A"})
	void shouldAcceptValidNames(String nameValue) {
		BoardName name = new BoardName(nameValue);
		assertEquals(nameValue, name.value());
	}

	@ParameterizedTest
	@ValueSource(strings = {"My Board #1", "Board-Name_123"})
	void shouldAcceptNamesWithSpecialCharacters(String nameValue) {
		BoardName name = new BoardName(nameValue);
		assertEquals(nameValue, name.value());
	}

	@Test
	void equals_shouldReturnTrueForSameValue() {
		BoardName name1 = new BoardName("Test Board");
		BoardName name2 = new BoardName("Test Board");

		assertEquals(name1, name2);
	}

	@Test
	void equals_shouldReturnFalseForDifferentValues() {
		BoardName name1 = new BoardName("Test Board");
		BoardName name2 = new BoardName("Other Board");

		assertNotEquals(name1, name2);
	}

	@Test
	void hashCode_shouldBeEqualForSameValue() {
		BoardName name1 = new BoardName("Test Board");
		BoardName name2 = new BoardName("Test Board");

		assertEquals(name1.hashCode(), name2.hashCode());
	}

	@Test
	void toString_shouldReturnFormattedString() {
		BoardName name = new BoardName("Test Board");
		String result = name.toString();

		assertEquals("BoardName{value='Test Board'}", result);
	}
}
