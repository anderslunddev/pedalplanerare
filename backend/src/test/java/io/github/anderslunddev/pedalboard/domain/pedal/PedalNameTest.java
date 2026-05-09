package io.github.anderslunddev.pedalboard.domain.pedal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedalNameTest {

	@Test
	void acceptsTypicalPedalName() {
		PedalName name = new PedalName("Big Muff Pi");
		assertEquals("Big Muff Pi", name.value());
	}

	@Test
	void acceptsNameAtMaxLength() {
		String value = "a".repeat(PedalName.MAX_LENGTH);
		PedalName name = new PedalName(value);
		assertEquals(value, name.value());
	}

	@Test
	void rejectsNameLongerThanMaxLength() {
		String value = "a".repeat(PedalName.MAX_LENGTH + 1);
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new PedalName(value));
		assertTrue(ex.getMessage().contains(String.valueOf(PedalName.MAX_LENGTH)),
				"message should mention the max length, was: " + ex.getMessage());
	}

	@Test
	void rejectsNullName() {
		NullPointerException ex = assertThrows(NullPointerException.class, () -> new PedalName(null));
		assertNotNull(ex.getMessage());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t", "\n", "   "})
	void rejectsBlankName(String value) {
		assertThrows(RuntimeException.class, () -> new PedalName(value));
	}
}
