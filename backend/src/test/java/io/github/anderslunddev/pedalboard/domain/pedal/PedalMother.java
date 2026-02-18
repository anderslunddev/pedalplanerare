package io.github.anderslunddev.pedalboard.domain.pedal;

import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;

import java.util.UUID;

/**
 * Object Mother for {@link Pedal} record used in tests.
 */
public final class PedalMother {

	private PedalMother() {
	}

	public static Pedal simple() {
		return new Pedal(new PedalId(UUID.randomUUID()), UUID.randomUUID(), new PedalName("Pedal"),
				new SurfaceArea(10.0, 5.0), new Color("#ffffff"), new Coordinate(1.0, 2.0), new Placement(1));
	}

	public static Pedal withPlacement(int placement) {
		Pedal base = simple();
		return new Pedal(base.id(), base.boardId(), base.name(), base.surfaceArea(), base.color(), base.coordinate(),
				new Placement(placement));
	}

	public static Pedal onBoard(UUID boardId, int placement) {
		Pedal base = simple();
		return new Pedal(base.id(), boardId, base.name(), base.surfaceArea(), base.color(), base.coordinate(),
				new Placement(placement));
	}
}

