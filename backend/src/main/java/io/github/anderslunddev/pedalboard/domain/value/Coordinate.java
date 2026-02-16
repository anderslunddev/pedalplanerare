package io.github.anderslunddev.pedalboard.domain.value;

public record Coordinate(Double x, Double y) {
	public Coordinate {
		if (x == null || y == null || x < 0 || y < 0) {
			throw new IllegalArgumentException("Coordinate values must be non-null and non-negative");
		}
	}
}
