package com.example.pedalboard.service.cable.calculation;

import com.example.pedalboard.domain.cable.PathPoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
final class PathLengthCalculator {

	double computeLength(List<PathPoint> points) {
		if (points.size() < 2)
			return 0.0;

		double length = 0.0;
		for (int i = 1; i < points.size(); i++) {
			PathPoint a = points.get(i - 1);
			PathPoint b = points.get(i);
			double dx = b.x() - a.x();
			double dy = b.y() - a.y();
			length += Math.sqrt(dx * dx + dy * dy);
		}

		return length;
	}
}
