package io.github.anderslunddev.pedalboard.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

	@Test
	void parse_acceptsTypicalAddress_andNormalizesCase() {
		assertThat(Email.parse("  User@Example.COM  ").value()).isEqualTo("user@example.com");
	}

	@Test
	void parse_rejectsBlank() {
		assertThatThrownBy(() -> Email.parse(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blank");
		assertThatThrownBy(() -> Email.parse("   ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blank");
	}

	@Test
	void parse_rejectsWithoutAt() {
		assertThatThrownBy(() -> Email.parse("not-an-email")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("valid email");
	}

	@Test
	void parse_rejectsMultipleAt() {
		assertThatThrownBy(() -> Email.parse("a@b@c.com")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("valid email");
	}

	@Test
	void parse_rejectsDomainWithoutTld() {
		assertThatThrownBy(() -> Email.parse("user@localhost")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("valid email");
	}

	@Test
	void equals_andHashCode_byValue() {
		Email a = Email.parse("a@b.co");
		Email b = Email.parse("A@B.CO");
		assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
	}
}
