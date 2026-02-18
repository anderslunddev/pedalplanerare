package io.github.anderslunddev.pedalboard.domain.value;

public record SurfaceArea(Double width, Double height) {
	public SurfaceArea {
		if (width == null || width <= 0 || height == null || height <= 0) {
			throw new IllegalArgumentException("Surface area dimensions must be greater than zero");
		}
	}

	/**
	 * Returns true if this rectangle, positioned at {@code thisTopLeft}, overlaps
	 * the {@code other} rectangle positioned at {@code otherTopLeft}.
	 */
	public boolean overlapsWith(SurfaceArea other, Coordinate thisTopLeft, Coordinate otherTopLeft) {
		double ax = thisTopLeft.x();
		double ay = thisTopLeft.y();
		double aRight = ax + width;
		double aBottom = ay + height;

		double bx = otherTopLeft.x();
		double by = otherTopLeft.y();
		double bRight = bx + other.width();
		double bBottom = by + other.height();

		// overlap if not (a is left of b OR b is left of a OR a is above b OR b is above a)
		return !(aRight <= bx || bRight <= ax || aBottom <= by || bBottom <= ay);
	}
}
