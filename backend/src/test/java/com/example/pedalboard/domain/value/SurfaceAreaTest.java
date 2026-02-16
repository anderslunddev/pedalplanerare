package com.example.pedalboard.domain.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurfaceAreaTest {

	@Test
	void widthAndHeightMustBeStrictlyPositive() {
		assertThrows(IllegalArgumentException.class, () -> new SurfaceArea(0.0, 10.0));
		assertThrows(IllegalArgumentException.class, () -> new SurfaceArea(-1.0, 10.0));
		assertThrows(IllegalArgumentException.class, () -> new SurfaceArea(10.0, 0.0));
		assertThrows(IllegalArgumentException.class, () -> new SurfaceArea(10.0, -1.0));
	}

	@Test
	void widthAndHeightMustNotBeNull() {
		assertThrows(IllegalArgumentException.class, () -> new SurfaceArea(null, 10.0));
		assertThrows(IllegalArgumentException.class, () -> new SurfaceArea(10.0, null));
	}

	@Test
	void constructsWithValidValues() {
		SurfaceArea area = new SurfaceArea(10.0, 20.0);
		assertEquals(10.0, area.width());
		assertEquals(20.0, area.height());
	}
}
