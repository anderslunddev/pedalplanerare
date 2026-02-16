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

class AStarRouterTest {

	private final AStarRouter router = new AStarRouter();

	@Test
	void findsPathOnEmptyGrid() {
		UUID boardId = UUID.randomUUID();
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(boardId, "src", 0.25, 0.25, 0.5, 0.5);
		Pedal dest = pedal(boardId, "dst", 4.25, 4.25, 0.5, 0.5);
		Grid grid = new Grid(board, List.of(source, dest), source, dest);

		Cell start = grid.toCell(new Point(0.5, 0.5));
		Cell goal = grid.toCell(new Point(4.5, 4.5));

		List<Cell> path = router.findPath(start, goal, grid);

		assertFalse(path.isEmpty());
		assertEquals(start.x(), path.get(0).x());
		assertEquals(start.y(), path.get(0).y());
		assertEquals(goal.x(), path.get(path.size() - 1).x());
		assertEquals(goal.y(), path.get(path.size() - 1).y());
	}

	@Test
	void findsPathAroundObstacle() {
		UUID boardId = UUID.randomUUID();
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(boardId, "src", 0.25, 2.25, 0.5, 0.5);
		Pedal dest = pedal(boardId, "dst", 4.25, 2.25, 0.5, 0.5);
		Pedal obstacle = pedal(boardId, "obs", 2.0, 2.0, 1.0, 1.0);
		Grid grid = new Grid(board, List.of(source, dest, obstacle), source, dest);

		Cell start = grid.toCell(new Point(0.5, 2.5));
		Cell goal = grid.toCell(new Point(4.5, 2.5));

		List<Cell> path = router.findPath(start, goal, grid);

		assertFalse(path.isEmpty());
		assertEquals(start.x(), path.get(0).x());
		assertEquals(goal.x(), path.get(path.size() - 1).x());
		assertTrue(path.size() > 2, "Path should go around obstacle");
	}

	@Test
	void returnsEmptyWhenNoPathExists() {
		UUID boardId = UUID.randomUUID();
		Board board = new Board(new BoardId(boardId), new UserId(UUID.randomUUID()), new BoardName("B"),
				new SurfaceArea(5.0, 5.0), List.of());
		Pedal source = pedal(boardId, "src", 0.25, 2.25, 0.5, 0.5);
		Pedal dest = pedal(boardId, "dst", 4.25, 2.25, 0.5, 0.5);
		// Full-height wall separating left and right
		Pedal wall = pedal(boardId, "wall", 2.0, 0.0, 1.0, 5.0);
		Grid grid = new Grid(board, List.of(source, dest, wall), source, dest);

		Cell start = grid.toCell(new Point(0.5, 2.5));
		Cell goal = grid.toCell(new Point(4.5, 2.5));

		List<Cell> path = router.findPath(start, goal, grid);

		assertTrue(path.isEmpty());
	}

	private static Pedal pedal(UUID boardId, String name, double x, double y, double w, double h) {
		return new Pedal(new PedalId(UUID.randomUUID()), boardId, new PedalName(name), new SurfaceArea(w, h),
				new Color("#000000"), new Coordinate(x, y), new Placement(1));
	}
}
