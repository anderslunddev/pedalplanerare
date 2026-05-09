package io.github.anderslunddev.pedalboard.domain.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardNameTest {

	@Test
	void acceptsTypicalBoardName() {
		BoardName name = new BoardName("My Pedalboard");
		assertEquals("My Pedalboard", name.value());
	}

	@Test
	void acceptsNameAtMaxLength() {
		String value = "a".repeat(BoardName.MAX_LENGTH);
		BoardName name = new BoardName(value);
		assertEquals(value, name.value());
	}

	@Test
	void rejectsNameLongerThanMaxLength() {
		String value = "a".repeat(BoardName.MAX_LENGTH + 1);
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BoardName(value));
		assertTrue(ex.getMessage().contains(String.valueOf(BoardName.MAX_LENGTH)),
				"message should mention the max length, was: " + ex.getMessage());
	}

	@Test
	void rejectsNullName() {
		NullPointerException ex = assertThrows(NullPointerException.class, () -> new BoardName(null));
		assertNotNull(ex.getMessage());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t", "\n", "   "})
	void rejectsBlankName(String value) {
		assertThrows(RuntimeException.class, () -> new BoardName(value));
	}
}
