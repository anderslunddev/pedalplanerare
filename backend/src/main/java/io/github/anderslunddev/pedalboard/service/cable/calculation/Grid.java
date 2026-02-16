package io.github.anderslunddev.pedalboard.service.cable.calculation;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;

import java.util.List;

final class Grid {

	private static final double CELL_SIZE = 0.5; // 0.5 cm

	private final int widthCells;
	private final int heightCells;
	private final boolean[][] blocked;

	Grid(Board board, List<Pedal> allPedals, Pedal source, Pedal destination) {
		this.widthCells = Math.max(1, (int) Math.round(board.surfaceArea().width() / CELL_SIZE));
		this.heightCells = Math.max(1, (int) Math.round(board.surfaceArea().height() / CELL_SIZE));
		this.blocked = new boolean[widthCells][heightCells];

		for (Pedal p : allPedals) {
			// Do not treat the source/destination pedals themselves as obstacles
			boolean isEndpoint = p.id().equals(source.id()) || p.id().equals(destination.id());
			if (!isEndpoint) {
				rasterizePedal(p);
			}
		}
	}

	private void rasterizePedal(Pedal pedal) {
		double xUnits = pedal.coordinate().x();
		double yUnits = pedal.coordinate().y();
		double wUnits = pedal.surfaceArea().width();
		double hUnits = pedal.surfaceArea().height();

		int x1 = clamp((int) Math.floor(xUnits / CELL_SIZE) - 1, 0, widthCells - 1);
		int y1 = clamp((int) Math.floor(yUnits / CELL_SIZE) - 1, 0, heightCells - 1);
		int x2 = clamp((int) Math.ceil((xUnits + wUnits) / CELL_SIZE) + 1, 0, widthCells);
		int y2 = clamp((int) Math.ceil((yUnits + hUnits) / CELL_SIZE) + 1, 0, heightCells);
		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				blocked[x][y] = true;
			}
		}
	}

	void clearCell(Cell cell) {
		if (inBounds(cell)) {
			blocked[cell.x()][cell.y()] = false;
		}
	}

	boolean isBlocked(Cell cell) {
		if (!inBounds(cell)) {
			return true;
		}
		return blocked[cell.x()][cell.y()];
	}

	boolean inBounds(Cell cell) {
		return cell.x() >= 0 && cell.y() >= 0 && cell.x() < widthCells && cell.y() < heightCells;
	}

	int width() {
		return widthCells;
	}

	int height() {
		return heightCells;
	}

	Cell toCell(Point point) {
		int x = (int) Math.floor(point.x() / CELL_SIZE);
		int y = (int) Math.floor(point.y() / CELL_SIZE);
		return new Cell(x, y);
	}

	Point toPoint(Cell cell) {
		double xUnits = (cell.x() + 0.5) * CELL_SIZE;
		double yUnits = (cell.y() + 0.5) * CELL_SIZE;
		return new Point(xUnits, yUnits);
	}

	private static int clamp(int v, int min, int max) {
		if (v < min) {
			return min;
		}
		if (v > max) {
			return max;
		}
		return v;
	}
}
