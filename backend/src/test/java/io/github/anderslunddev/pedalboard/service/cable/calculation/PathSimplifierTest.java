package io.github.anderslunddev.pedalboard.service.cable.calculation;

import io.github.anderslunddev.pedalboard.domain.cable.PathPoint;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathSimplifierTest {

	private final PathSimplifier simplifier = new PathSimplifier(new Geometry());

	@Test
	void leavesTwoPointsUnchanged() {
		List<PathPoint> path = List.of(new PathPoint(0, 0), new PathPoint(10, 10));
		Pedal source = pedal(0, 0);
		Pedal dest = pedal(10, 10);
		List<PathPoint> result = simplifier.simplifyByLineOfSight(path, List.of(source, dest), source, dest);
		assertEquals(2, result.size());
		assertEquals(path.get(0).x(), result.get(0).x());
		assertEquals(path.get(1).x(), result.get(1).x());
	}

	@Test
	void simplifiesCollinearPathWhenNoObstacleBetween() {
		List<PathPoint> path = List.of(new PathPoint(0, 0), new PathPoint(5, 5), new PathPoint(10, 10));
		Pedal source = pedal(0, 0);
		Pedal dest = pedal(10, 10);
		List<PathPoint> result = simplifier.simplifyByLineOfSight(path, List.of(source, dest), source, dest);
		assertEquals(2, result.size());
		assertEquals(0, result.get(0).x());
		assertEquals(10, result.get(1).x());
	}

	@Test
	void keepsWaypointsWhenObstacleBlocksLineOfSight() {
		List<PathPoint> path = List.of(new PathPoint(0, 5), new PathPoint(5, 5), new PathPoint(10, 5));
		Pedal source = pedal(0, 5);
		Pedal dest = pedal(10, 5);
		Pedal obstacle = pedal(5, 4, 2, 2);
		List<PathPoint> result = simplifier.simplifyByLineOfSight(path, List.of(source, dest, obstacle), source, dest);
		assertEquals(3, result.size());
	}

	private static Pedal pedal(double x, double y) {
		return pedal(x, y, 0.5, 0.5);
	}

	private static Pedal pedal(double x, double y, double w, double h) {
		return new Pedal(new PedalId(UUID.randomUUID()), new PedalName("p"), new SurfaceArea(w, h),
				new Color("#000000"), new Coordinate(x, y), new Placement(1));
	}
}
