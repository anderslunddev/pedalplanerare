package com.example.pedalboard.service.cable.calculation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeometryTest {

	private Geometry geometry;

	@BeforeEach
	void setUp() {
		geometry = new Geometry();
	}

	@Nested
	class PointInRect {

		@Test
		void returnsTrueWhenPointIsInside() {
			Rect rect = new Rect(10, 10, 20, 20);
			assertTrue(geometry.pointInRect(new Point(15, 15), rect));
			assertTrue(geometry.pointInRect(new Point(10.5, 10.5), rect));
			assertTrue(geometry.pointInRect(new Point(29, 29), rect));
		}

		@Test
		void returnsTrueWhenPointIsOnEdge() {
			Rect rect = new Rect(10, 10, 20, 20);
			assertTrue(geometry.pointInRect(new Point(10, 10), rect));
			assertTrue(geometry.pointInRect(new Point(30, 10), rect));
			assertTrue(geometry.pointInRect(new Point(10, 30), rect));
			assertTrue(geometry.pointInRect(new Point(30, 30), rect));
		}

		@Test
		void returnsFalseWhenPointIsOutside() {
			Rect rect = new Rect(10, 10, 20, 20);
			assertFalse(geometry.pointInRect(new Point(9, 15), rect));
			assertFalse(geometry.pointInRect(new Point(31, 15), rect));
			assertFalse(geometry.pointInRect(new Point(15, 9), rect));
			assertFalse(geometry.pointInRect(new Point(15, 31), rect));
		}
	}

	@Nested
	class SegmentsIntersect {

		@Test
		void returnsTrueWhenSegmentsCross() {
			Line line1 = new Line(new Point(0, 0), new Point(10, 10));
			Line line2 = new Line(new Point(0, 10), new Point(10, 0));
			assertTrue(geometry.segmentsIntersect(line1, line2));
		}

		@Test
		void returnsTrueWhenOneSegmentEndTouchesTheOther() {
			Line line1 = new Line(new Point(0, 0), new Point(10, 0));
			Line line2 = new Line(new Point(10, 0), new Point(10, 10));
			assertTrue(geometry.segmentsIntersect(line1, line2));
		}

		@Test
		void returnsFalseWhenSegmentsAreParallelAndDisjoint() {
			Line line1 = new Line(new Point(0, 0), new Point(10, 0));
			Line line2 = new Line(new Point(0, 5), new Point(10, 5));
			assertFalse(geometry.segmentsIntersect(line1, line2));
		}

		@Test
		void returnsFalseWhenSegmentsDoNotMeet() {
			Line line1 = new Line(new Point(0, 0), new Point(2, 2));
			Line line2 = new Line(new Point(5, 0), new Point(7, 2));
			assertFalse(geometry.segmentsIntersect(line1, line2));
		}
	}

	@Nested
	class SegmentIntersectsRect {

		@Test
		void returnsTrueWhenSegmentCrossesRect() {
			Rect rect = new Rect(10, 10, 10, 10);
			Line line = new Line(new Point(5, 15), new Point(25, 15));
			assertTrue(geometry.segmentIntersectsRect(line, rect));
		}

		@Test
		void returnsTrueWhenSegmentStartsInsideRect() {
			Rect rect = new Rect(10, 10, 10, 10);
			Line line = new Line(new Point(15, 15), new Point(30, 30));
			assertTrue(geometry.segmentIntersectsRect(line, rect));
		}

		@Test
		void returnsFalseWhenSegmentIsFullyOutsideRect() {
			Rect rect = new Rect(10, 10, 10, 10);
			Line line = new Line(new Point(0, 0), new Point(5, 5));
			assertFalse(geometry.segmentIntersectsRect(line, rect));
		}

		@Test
		void returnsFalseWhenSegmentRunsAlongsideRect() {
			Rect rect = new Rect(10, 10, 10, 10);
			Line line = new Line(new Point(0, 5), new Point(25, 5));
			assertFalse(geometry.segmentIntersectsRect(line, rect));
		}
	}
}
