package io.github.anderslunddev.pedalboard.domain.board;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;

import java.util.List;
import java.util.UUID;

/**
 * Object Mother for {@link Board} to simplify record construction in tests.
 */
public final class BoardMother {

	private BoardMother() {
	}

	public static Board simple() {
		return new Board(new BoardId(UUID.randomUUID()), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(60.0, 30.0), List.of());
	}

	public static Board withName(String name) {
		Board base = simple();
		return new Board(base.id(), base.userId(), new BoardName(name), base.surfaceArea(), base.pedals());
	}

	public static Board withIdAndUser(BoardId id, UserId userId) {
		return new Board(id, userId, new BoardName("Test Board"), new SurfaceArea(60.0, 30.0), List.of());
	}

	public static Board withDimensions(double width, double height) {
		Board base = simple();
		return new Board(base.id(), base.userId(), base.name(), new SurfaceArea(width, height), base.pedals());
	}

	public static Board withPedals(List<Pedal> pedals) {
		Board base = simple();
		return new Board(base.id(), base.userId(), base.name(), base.surfaceArea(), List.copyOf(pedals));
	}
}

