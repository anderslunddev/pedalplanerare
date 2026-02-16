package io.github.anderslunddev.pedalboard.service.cable.calculation;

import org.springframework.stereotype.Component;

@Component
final class Geometry {

	boolean pointInRect(Point point, Rect rect) {
		return point.x() >= rect.x() && point.x() <= rect.right() && point.y() >= rect.y()
				&& point.y() <= rect.bottom();
	}

	boolean segmentsIntersect(Line line1, Line line2) {
		Point a = line1.a();
		Point b = line1.b();
		Point c = line2.a();
		Point d = line2.b();

		double d1 = direction(c, d, a);
		double d2 = direction(c, d, b);
		double d3 = direction(a, b, c);
		double d4 = direction(a, b, d);

		if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
			return true;
		}

		return d1 == 0 && onSegment(c, d, a) || d2 == 0 && onSegment(c, d, b) || d3 == 0 && onSegment(a, b, c)
				|| d4 == 0 && onSegment(a, b, d);
	}

	boolean segmentIntersectsRect(Line line, Rect rect) {
		Point a = line.a();
		Point b = line.b();

		// If both points are on one side of rectangle, no intersection
		if (a.x() < rect.x() && b.x() < rect.x())
			return false;
		if (a.x() > rect.right() && b.x() > rect.right())
			return false;
		if (a.y() < rect.y() && b.y() < rect.y())
			return false;
		if (a.y() > rect.bottom() && b.y() > rect.bottom())
			return false;

		// If either point is inside the rectangle, we consider it intersecting
		if (pointInRect(a, rect) || pointInRect(b, rect)) {
			return true;
		}

		// Check intersection with rectangle edges
		Point topLeft = new Point(rect.x(), rect.y());
		Point topRight = new Point(rect.right(), rect.y());
		Point bottomRight = new Point(rect.right(), rect.bottom());
		Point bottomLeft = new Point(rect.x(), rect.bottom());

		return this.segmentsIntersect(line, new Line(topLeft, topRight))
				|| this.segmentsIntersect(line, new Line(topRight, bottomRight))
				|| this.segmentsIntersect(line, new Line(bottomRight, bottomLeft))
				|| this.segmentsIntersect(line, new Line(bottomLeft, topLeft));
	}

	private static double direction(Point a, Point b, Point c) {
		return (c.x() - a.x()) * (b.y() - a.y()) - (b.x() - a.x()) * (c.y() - a.y());
	}

	private static boolean onSegment(Point a, Point b, Point c) {
		return Math.min(a.x(), b.x()) <= c.x() && c.x() <= Math.max(a.x(), b.x()) && Math.min(a.y(), b.y()) <= c.y()
				&& c.y() <= Math.max(a.y(), b.y());
	}
}
