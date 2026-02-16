package io.github.anderslunddev.pedalboard.domain.board;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardTest {

	@Test
	void shouldThrowExceptionForNullId() {
		BoardName name = new BoardName("Test Board");
		assertThrows(NullPointerException.class,
				() -> new Board(null, new UserId(UUID.randomUUID()), name, new SurfaceArea(10.0, 5.0), List.of()));
	}

	@Test
	void shouldThrowExceptionForNullName() {
		UUID id = UUID.randomUUID();
		assertThrows(NullPointerException.class, () -> new Board(new BoardId(id), new UserId(UUID.randomUUID()), null,
				new SurfaceArea(10.0, 5.0), List.of()));
	}

	@Test
	void shouldThrowExceptionForBlankName() {
		UUID id = UUID.randomUUID();
		assertThrows(IllegalArgumentException.class, () -> new BoardName(""));
		assertThrows(IllegalArgumentException.class, () -> new BoardName("   "));
	}

	@Test
	void shouldConstruct() {
		UUID id = UUID.randomUUID();
		BoardName name = new BoardName("Test Board");
		double width = 60.0;
		double height = 30.0;
		SurfaceArea surfaceArea = new SurfaceArea(width, height);

		BoardId boardId = new BoardId(id);
		UserId userId = new UserId(UUID.randomUUID());

		Board board = new Board(boardId, userId, name, surfaceArea, List.of());

		assertEquals(boardId, board.id());
		assertEquals(name, board.name());
		assertEquals(width, board.surfaceArea().width());
		assertEquals(height, board.surfaceArea().height());
		assertEquals(List.of(), board.pedals());
	}

	@Test
	void resolvePlacement_shouldReturn1_whenNoExistingPedalsAndNoPlacementRequested() {
		UUID boardId = UUID.randomUUID();
		Board boardWithNoPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), List.of());
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), null);

		Placement result = boardWithNoPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(1, result.value());
	}

	@Test
	void resolvePlacement_shouldReturnMaxPlus1_whenExistingPedalsAndNoPlacementRequested() {
		UUID boardId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		List<Pedal> existingPedals = List.of(createPedal(id1, boardId, 1), createPedal(id2, boardId, 3));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), null);

		Placement result = boardWithPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(4, result.value()); // max(1, 3) + 1
	}

	@Test
	void resolvePlacement_shouldReturnRequestedPlacement_whenValidAndNotTaken() {
		UUID boardId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		List<Pedal> existingPedals = List.of(createPedal(id1, boardId, 1), createPedal(id2, boardId, 2));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), new Placement(5));

		Placement result = boardWithPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(5, result.value());
	}

	@Test
	void resolvePlacement_shouldThrowException_whenRequestedPlacementIsAlreadyTaken() {
		UUID boardId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		List<Pedal> existingPedals = List.of(createPedal(id1, boardId, 1), createPedal(id2, boardId, 3));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), new Placement(3));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> boardWithPedals.resolvePlacementFor(pedalToCreate));

		assertEquals("Placement number 3 is already in use.", exception.getMessage());
	}

	@Test
	void resolvePlacement_shouldReturnMaxPlus1_whenPlacementIsZero() {
		UUID boardId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		List<Pedal> existingPedals = List.of(createPedal(id1, boardId, 2));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), null);

		Placement result = boardWithPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(3, result.value()); // max(2) + 1, because null is treated as "no placement requested"
	}

	@Test
	void resolvePlacement_shouldReturnMaxPlus1_whenPlacementIsNegative() {
		UUID boardId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		List<Pedal> existingPedals = List.of(createPedal(id1, boardId, 1));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), null);

		Placement result = boardWithPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(2, result.value()); // max(1) + 1, because null is treated as "no placement requested"
	}

	@Test
	void resolvePlacement_shouldReturn1_whenEmptyListAndPlacementIsZero() {
		UUID boardId = UUID.randomUUID();
		Board boardWithNoPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), List.of());
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), null);

		Placement result = boardWithNoPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(1, result.value());
	}

	@Test
	void resolvePlacement_shouldAllowPlacement1_whenNoExistingPedals() {
		UUID boardId = UUID.randomUUID();
		Board boardWithNoPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), List.of());
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), new Placement(1));

		Placement result = boardWithNoPedals.resolvePlacementFor(pedalToCreate);

		assertEquals(1, result.value());
	}

	@Test
	void resolvePlacement_shouldThrowException_whenPlacement1IsAlreadyTaken() {
		UUID boardId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		List<Pedal> existingPedals = List.of(createPedal(id1, boardId, 1));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), new Placement(1));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> boardWithPedals.resolvePlacementFor(pedalToCreate));

		assertEquals("Placement number 1 is already in use.", exception.getMessage());
	}

	private static Pedal createPedal(UUID id, UUID boardId, int placement) {
		return new Pedal(new PedalId(id), boardId, new PedalName("Pedal" + placement), new SurfaceArea(10.0, 10.0),
				new Color("#000000"), new Coordinate(0.0, 0.0), new Placement(placement));
	}
}
