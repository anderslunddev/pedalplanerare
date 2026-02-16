package com.example.pedalboard.service.cable.calculation;

import com.example.pedalboard.domain.board.Board;
import com.example.pedalboard.domain.cable.PathPoint;
import com.example.pedalboard.domain.pedal.Pedal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RoutingService {

	private final AStarRouter router;
	private final PathLengthCalculator pathLengthCalculator;
	private final PathSimplifier pathSimplifier;

	RoutingService(AStarRouter router, PathLengthCalculator pathLengthCalculator, PathSimplifier pathSimplifier) {
		this.router = router;
		this.pathLengthCalculator = pathLengthCalculator;
		this.pathSimplifier = pathSimplifier;
	}

	public RoutingResult route(Board board, Pedal source, Pedal destination, List<Pedal> allPedals) {
		// Build grid
		Grid grid = new Grid(board, allPedals, source, destination);

		// Compute start/goal points
		Point startPoint = computeStartPoint(source);
		Point goalPoint = computeGoalPoint(destination);
		Cell start = grid.toCell(startPoint);
		Cell goal = grid.toCell(goalPoint);

		// Allow start/goal even if inside blocked
		grid.clearCell(start);
		grid.clearCell(goal);

		// Run router
		List<Cell> cellPath = router.findPath(start, goal, grid);

		// Handle no path found - fallback to direct line
		if (cellPath.isEmpty()) {
			List<PathPoint> direct = Arrays.asList(new PathPoint(startPoint.x(), startPoint.y()),
					new PathPoint(goalPoint.x(), goalPoint.y()));
			double length = pathLengthCalculator.computeLength(direct);
			return new RoutingResult(direct, length);
		}

		// Convert cells to path points
		List<PathPoint> points = convertCellsToPathPoints(cellPath, grid);

		// Simplify path
		List<PathPoint> simplified = pathSimplifier.simplifyByLineOfSight(points, allPedals, source, destination);

		// Compute length
		double length = pathLengthCalculator.computeLength(simplified);

		return new RoutingResult(simplified, length);
	}

	private static Point computeStartPoint(Pedal source) {
		double sxUnits = source.coordinate().x() + source.surfaceArea().width();
		double syUnits = source.coordinate().y() + source.surfaceArea().height() / 2.0;
		return new Point(sxUnits, syUnits);
	}

	private static Point computeGoalPoint(Pedal destination) {
		double dxUnits = destination.coordinate().x();
		double dyUnits = destination.coordinate().y() + destination.surfaceArea().height() / 2.0;
		return new Point(dxUnits, dyUnits);
	}

	private static List<PathPoint> convertCellsToPathPoints(List<Cell> cellPath, Grid grid) {
		List<PathPoint> points = new ArrayList<>();
		for (Cell cell : cellPath) {
			Point point = grid.toPoint(cell);
			points.add(new PathPoint(point.x(), point.y()));
		}
		return points;
	}

	public record RoutingResult(List<PathPoint> pathPoints, double totalLength) {
	}
}
