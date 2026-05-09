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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		List<Pedal> existingPedals = List.of(createPedal(id1, 1), createPedal(id2, 3));
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
		List<Pedal> existingPedals = List.of(createPedal(id1, 1), createPedal(id2, 2));
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
		List<Pedal> existingPedals = List.of(createPedal(id1, 1), createPedal(id2, 3));
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
		List<Pedal> existingPedals = List.of(createPedal(id1, 2));
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
		List<Pedal> existingPedals = List.of(createPedal(id1, 1));
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
		List<Pedal> existingPedals = List.of(createPedal(id1, 1));
		Board boardWithPedals = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()),
				new BoardName("Test Board"), new SurfaceArea(100.0, 50.0), existingPedals);
		PedalToCreate pedalToCreate = new PedalToCreate(new PedalName("Test"), new SurfaceArea(10.0, 10.0),
				new Color("#ffffff"), new Coordinate(0.0, 0.0), new Placement(1));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> boardWithPedals.resolvePlacementFor(pedalToCreate));

		assertEquals("Placement number 1 is already in use.", exception.getMessage());
	}

	private static Pedal createPedal(UUID id, int placement) {
		return new Pedal(new PedalId(id), new PedalName("Pedal" + placement), new SurfaceArea(10.0, 10.0),
				new Color("#000000"), new Coordinate(0.0, 0.0), new Placement(placement));
	}

	private static Pedal createPedalAt(UUID id, int placement, double x, double y, double width,
			double height) {
		return new Pedal(new PedalId(id), new PedalName("Pedal"), new SurfaceArea(width, height),
				new Color("#000000"), new Coordinate(x, y), new Placement(placement));
	}

	// --- wouldOverlapWithExisting ---

	@Test
	void wouldOverlapWithExisting_returnsFalse_whenNoExistingPedals() {
		UUID boardId = UUID.randomUUID();
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of());
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(25.0, 15.0), null);

		assertFalse(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsFalse_whenNewPedalIsLeftOfExisting() {
		UUID boardId = UUID.randomUUID();
		// Existing: x=20, y=10, 10x10 → right edge 30, bottom 20
		Pedal existing = createPedalAt(UUID.randomUUID(), 1, 20.0, 10.0, 10.0, 10.0);
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(existing));
		// New: x=5, y=10, 10x10 → right edge 15, so 15 <= 20 (existing left) → no overlap
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(5.0, 10.0), null);

		assertFalse(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsFalse_whenNewPedalIsRightOfExisting() {
		UUID boardId = UUID.randomUUID();
		Pedal existing = createPedalAt(UUID.randomUUID(), 1, 10.0, 10.0, 10.0, 10.0); // 10–20, 10–20
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(existing));
		// New: x=25, right edge 35; existing right 20; 20 <= 25 → no overlap
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(25.0, 10.0), null);

		assertFalse(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsFalse_whenNewPedalIsAboveExisting() {
		UUID boardId = UUID.randomUUID();
		Pedal existing = createPedalAt(UUID.randomUUID(), 1, 10.0, 20.0, 10.0, 10.0); // y 20–30
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(existing));
		// New: y=5, height 10 → bottom 15; 15 <= 20 → no overlap
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(10.0, 5.0), null);

		assertFalse(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsFalse_whenNewPedalIsBelowExisting() {
		UUID boardId = UUID.randomUUID();
		Pedal existing = createPedalAt(UUID.randomUUID(), 1, 10.0, 5.0, 10.0, 10.0); // y 5–15
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(existing));
		// New: y=20, bottom 30; existing bottom 15; 15 <= 20 → no overlap
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(10.0, 20.0), null);

		assertFalse(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsTrue_whenNewPedalAtSamePosition() {
		UUID boardId = UUID.randomUUID();
		Pedal existing = createPedalAt(UUID.randomUUID(), 1, 10.0, 10.0, 10.0, 10.0);
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(existing));
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(10.0, 10.0), null);

		assertTrue(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsTrue_whenNewPedalPartiallyOverlaps() {
		UUID boardId = UUID.randomUUID();
		// Existing: 10–20, 10–20
		Pedal existing = createPedalAt(UUID.randomUUID(), 1, 10.0, 10.0, 10.0, 10.0);
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(existing));
		// New: 15–25, 15–25 → overlaps in 15–20, 15–20
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(15.0, 15.0), null);

		assertTrue(board.wouldOverlapWithExisting(toCreate));
	}

	@Test
	void wouldOverlapWithExisting_returnsTrue_whenOverlappingSecondOfTwoPedals() {
		UUID boardId = UUID.randomUUID();
		Pedal p1 = createPedalAt(UUID.randomUUID(), 1, 0.0, 0.0, 10.0, 10.0);
		Pedal p2 = createPedalAt(UUID.randomUUID(), 2, 25.0, 10.0, 10.0, 10.0);
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("Test"),
				new SurfaceArea(100.0, 50.0), List.of(p1, p2));
		// New overlaps p2 (25–35, 10–20)
		PedalToCreate toCreate = new PedalToCreate(new PedalName("New"), new SurfaceArea(10.0, 10.0),
				new Color("#fff"), new Coordinate(25.0, 10.0), null);

		assertTrue(board.wouldOverlapWithExisting(toCreate));
	}
}
