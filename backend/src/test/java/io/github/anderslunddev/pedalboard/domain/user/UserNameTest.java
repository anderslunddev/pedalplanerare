package io.github.anderslunddev.pedalboard.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserNameTest {

	@Test
	void parse_trimsWhitespace() {
		assertThat(UserName.parse("  alice  ").value()).isEqualTo("alice");
	}

	@Test
	void parse_rejectsNull() {
		assertThatThrownBy(() -> UserName.parse(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blank");
	}

	@Test
	void parse_rejectsBlank() {
		assertThatThrownBy(() -> UserName.parse("   ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blank");
	}

	@Test
	void parse_rejectsTooLong() {
		String longName = "a".repeat(UserName.MAX_LENGTH + 1);
		assertThatThrownBy(() -> UserName.parse(longName)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("too long");
	}
}
