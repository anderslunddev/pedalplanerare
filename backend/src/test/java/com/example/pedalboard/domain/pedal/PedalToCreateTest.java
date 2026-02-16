package com.example.pedalboard.domain.pedal;

import com.example.pedalboard.domain.value.Color;
import com.example.pedalboard.domain.value.Coordinate;
import com.example.pedalboard.domain.value.SurfaceArea;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PedalToCreateTest {

	@Test
	void constructorRejectsNullRequiredFields() {
		SurfaceArea area = new SurfaceArea(10.0, 5.0);
		Color color = new Color("#ffffff");
		Coordinate coord = new Coordinate(1.0, 2.0);

		assertThrows(NullPointerException.class, () -> new PedalToCreate(null, area, color, coord, null));
		assertThrows(NullPointerException.class,
				() -> new PedalToCreate(new PedalName("Name"), null, color, coord, null));
		assertThrows(NullPointerException.class,
				() -> new PedalToCreate(new PedalName("Name"), area, null, coord, null));
		assertThrows(NullPointerException.class,
				() -> new PedalToCreate(new PedalName("Name"), area, color, null, null));
	}

	@Test
	void constructorAllowsNullPlacement() {
		PedalToCreate pedal = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 5.0), new Color("#ffffff"),
				new Coordinate(1.0, 2.0), null);

		assertTrue(pedal.getPlacement().isEmpty());
	}

	@Test
	void constructorExposesFieldsCorrectly() {
		PedalName name = new PedalName("Test");
		SurfaceArea area = new SurfaceArea(10.0, 5.0);
		Color color = new Color("#ffffff");
		Coordinate coord = new Coordinate(1.0, 2.0);
		Placement placement = new Placement(3);

		PedalToCreate pedal = new PedalToCreate(name, area, color, coord, placement);

		assertSame(name, pedal.getName());
		assertSame(area, pedal.getSurfaceArea());
		assertSame(color, pedal.getColor());
		assertSame(coord, pedal.getCoordinate());
		assertEquals(placement, pedal.getPlacement().orElseThrow());
	}

	@Test
	void builderBuildsEquivalentInstance() {
		PedalName name = new PedalName("Built");
		SurfaceArea area = new SurfaceArea(8.0, 4.0);
		Color color = new Color("#000000");
		Coordinate coord = new Coordinate(3.0, 4.0);
		Placement placement = new Placement(2);

		PedalToCreate fromBuilder = PedalToCreate.builder().name(name).surfaceArea(area).color(color).coordinate(coord)
				.placement(placement).build();

		assertSame(name, fromBuilder.getName());
		assertSame(area, fromBuilder.getSurfaceArea());
		assertSame(color, fromBuilder.getColor());
		assertSame(coord, fromBuilder.getCoordinate());
		assertEquals(placement, fromBuilder.getPlacement().orElseThrow());
	}

	@Test
	void builderAllowsOmittingPlacement() {
		PedalToCreate fromBuilder = PedalToCreate.builder().name(new PedalName("NoPlacement"))
				.surfaceArea(new SurfaceArea(8.0, 4.0)).color(new Color("#000000")).coordinate(new Coordinate(3.0, 4.0))
				.build();

		assertTrue(fromBuilder.getPlacement().isEmpty());
	}
}
