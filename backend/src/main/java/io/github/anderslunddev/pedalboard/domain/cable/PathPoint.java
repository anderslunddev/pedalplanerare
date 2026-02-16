package io.github.anderslunddev.pedalboard.domain.cable;

public record PathPoint(double x, double y) {

	public PathPoint {
		if (Double.isNaN(x) || Double.isInfinite(x)) {
			throw new IllegalArgumentException("PathPoint x must be a finite number");
		}
		if (Double.isNaN(y) || Double.isInfinite(y)) {
			throw new IllegalArgumentException("PathPoint y must be a finite number");
		}
	}
}
