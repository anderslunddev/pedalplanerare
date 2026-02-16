package com.example.pedalboard.service.cable.calculation;

import com.example.pedalboard.domain.board.Board;
import com.example.pedalboard.domain.board.BoardId;
import com.example.pedalboard.domain.board.BoardName;
import com.example.pedalboard.domain.pedal.Pedal;
import com.example.pedalboard.domain.pedal.PedalId;
import com.example.pedalboard.domain.pedal.PedalName;
import com.example.pedalboard.domain.pedal.Placement;
import com.example.pedalboard.domain.user.UserId;
import com.example.pedalboard.domain.value.Color;
import com.example.pedalboard.domain.value.SurfaceArea;
import com.example.pedalboard.domain.value.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

	@Test
	void inBoundsAcceptsCellsInsideGrid() {
		Board board = new Board(new BoardId(UUID.randomUUID()), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(0, 0, 0.5, 0.5);
		Pedal dest = pedal(4, 4, 0.5, 0.5);
		Grid grid = new Grid(board, List.of(source, dest), source, dest);

		assertTrue(grid.inBounds(new Cell(0, 0)));
		assertTrue(grid.inBounds(new Cell(9, 9)));
	}

	@Test
	void inBoundsRejectsCellsOutsideGrid() {
		Board board = new Board(new BoardId(UUID.randomUUID()), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(0, 0, 0.5, 0.5);
		Pedal dest = pedal(4, 4, 0.5, 0.5);
		Grid grid = new Grid(board, List.of(source, dest), source, dest);

		assertFalse(grid.inBounds(new Cell(-1, 0)));
		assertFalse(grid.inBounds(new Cell(0, -1)));
		assertFalse(grid.inBounds(new Cell(10, 0)));
		assertFalse(grid.inBounds(new Cell(0, 10)));
	}

	@Test
	void toCellAndToPointRoundTrip() {
		Board board = new Board(new BoardId(UUID.randomUUID()), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(0, 0, 0.5, 0.5);
		Pedal dest = pedal(4, 4, 0.5, 0.5);
		Grid grid = new Grid(board, List.of(source, dest), source, dest);

		Cell cell = new Cell(2, 3);
		Point point = grid.toPoint(cell);
		Cell back = grid.toCell(point);
		assertEquals(cell.x(), back.x());
		assertEquals(cell.y(), back.y());
	}

	@Test
	void obstaclePedalMarksCellsAsBlocked() {
		UUID boardId = UUID.randomUUID();
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(boardId, "src", 0, 0, 0.5, 0.5);
		Pedal dest = pedal(boardId, "dst", 4.5, 4.5, 0.5, 0.5);
		Pedal obstacle = pedal(boardId, "obs", 2.0, 2.0, 1.0, 1.0);
		Grid grid = new Grid(board, List.of(source, dest, obstacle), source, dest);

		Cell obstacleCell = grid.toCell(new Point(2.5, 2.5));
		assertTrue(grid.isBlocked(obstacleCell));
		assertFalse(grid.isBlocked(new Cell(0, 0)));
		assertFalse(grid.isBlocked(new Cell(9, 9)));
	}

	@Test
	void sourceAndDestinationAreNotBlocked() {
		UUID boardId = UUID.randomUUID();
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(boardId, "src", 0.25, 0.25, 0.5, 0.5);
		Pedal dest = pedal(boardId, "dst", 4.25, 4.25, 0.5, 0.5);
		Grid grid = new Grid(board, List.of(source, dest), source, dest);

		Cell startCell = grid.toCell(new Point(0.5, 0.5));
		Cell endCell = grid.toCell(new Point(4.5, 4.5));
		assertFalse(grid.isBlocked(startCell));
		assertFalse(grid.isBlocked(endCell));
	}

	private static Pedal pedal(double x, double y, double w, double h) {
		return pedal(UUID.randomUUID(), "p", x, y, w, h);
	}

	private static Pedal pedal(UUID boardId, String name, double x, double y, double w, double h) {
		return new Pedal(new PedalId(UUID.randomUUID()), boardId, new PedalName(name), new SurfaceArea(w, h),
				new Color("#000000"), new Coordinate(x, y), new Placement(1));
	}
}
