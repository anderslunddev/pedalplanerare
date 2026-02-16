package io.github.anderslunddev.pedalboard.service.cable.calculation;

import io.github.anderslunddev.pedalboard.domain.cable.PathPoint;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
final class PathSimplifier {

	private final Geometry geometry;

	PathSimplifier(Geometry geometry) {
		this.geometry = geometry;
	}

	List<PathPoint> simplifyByLineOfSight(List<PathPoint> points, List<Pedal> pedals, Pedal source, Pedal destination) {
		if (points.size() <= 2) {
			return points;
		}

		List<PathPoint> result = new ArrayList<>();
		int i = 0;

		while (i < points.size() - 1) {
			int furthestVisible = i + 1;
			for (int j = points.size() - 1; j > i + 1; j--) {
				PathPoint a = points.get(i);
				PathPoint b = points.get(j);
				if (hasLineOfSight(a, b, pedals, source, destination)) {
					furthestVisible = j;
					break;
				}
			}
			result.add(points.get(i));
			i = furthestVisible;
		}

		result.add(points.get(points.size() - 1));
		return result;
	}

	private boolean hasLineOfSight(PathPoint a, PathPoint b, List<Pedal> pedals, Pedal source, Pedal destination) {
		Line line = new Line(new Point(a.x(), a.y()), new Point(b.x(), b.y()));

		for (Pedal p : pedals) {
			boolean isEndpoint = p.id().equals(source.id()) || p.id().equals(destination.id());
			if (!isEndpoint) {
				Rect pedalRect = new Rect(p.coordinate().x(), p.coordinate().y(), p.surfaceArea().width(),
						p.surfaceArea().height());
				if (geometry.segmentIntersectsRect(line, pedalRect)) {
					return false;
				}
			}
		}
		return true;
	}
}
