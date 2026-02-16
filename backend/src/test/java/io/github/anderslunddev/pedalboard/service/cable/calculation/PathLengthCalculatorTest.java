package io.github.anderslunddev.pedalboard.service.cable.calculation;

import io.github.anderslunddev.pedalboard.domain.cable.PathPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathLengthCalculatorTest {

	private final PathLengthCalculator calculator = new PathLengthCalculator();

	@Test
	void returnsZeroForEmptyOrSinglePoint() {
		assertEquals(0.0, calculator.computeLength(List.of()));
		assertEquals(0.0, calculator.computeLength(List.of(new PathPoint(1, 1))));
	}

	@Test
	void computesStraightLineLength() {
		List<PathPoint> path = List.of(new PathPoint(0, 0), new PathPoint(3, 4));
		assertEquals(5.0, calculator.computeLength(path), 1e-9);
	}

	@Test
	void sumsMultipleSegments() {
		List<PathPoint> path = List.of(new PathPoint(0, 0), new PathPoint(3, 0), new PathPoint(3, 4));
		assertEquals(7.0, calculator.computeLength(path), 1e-9);
	}

	@Test
	void handlesHorizontalSegment() {
		List<PathPoint> path = List.of(new PathPoint(0, 0), new PathPoint(10, 0));
		assertEquals(10.0, calculator.computeLength(path), 1e-9);
	}

	@Test
	void handlesVerticalSegment() {
		List<PathPoint> path = List.of(new PathPoint(0, 0), new PathPoint(0, 10));
		assertEquals(10.0, calculator.computeLength(path), 1e-9);
	}
}
