package io.github.anderslunddev.pedalboard.service.cable.calculation;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
final class AStarRouter {

	List<Cell> findPath(Cell start, Cell goal, Grid grid) {
		PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
		Map<String, Node> allNodes = new HashMap<>();

		Node startNode = new Node(start.x(), start.y(), 0.0, heuristic(start, goal), null);
		open.add(startNode);
		allNodes.put(key(start.x(), start.y()), startNode);

		boolean[][] visited = new boolean[grid.width()][grid.height()];
		int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

		while (!open.isEmpty()) {
			Node current = open.poll();
			Cell currentCell = new Cell(current.x, current.y);

			if (current.x == goal.x() && current.y == goal.y()) {
				return reconstruct(current);
			}

			if (!grid.inBounds(currentCell)) {
				continue;
			}

			if (visited[current.x][current.y]) {
				continue;
			}

			visited[current.x][current.y] = true;

			for (int[] dir : directions) {
				int nx = current.x + dir[0];
				int ny = current.y + dir[1];
				Cell nextCell = new Cell(nx, ny);

				if (!grid.inBounds(nextCell)) {
					continue;
				}

				if (grid.isBlocked(nextCell)) {
					continue;
				}

				double g = current.g + 1.0;
				String k = key(nx, ny);
				Node existing = allNodes.get(k);
				double h = heuristic(nextCell, goal);

				if (existing == null || g < existing.g) {
					Node next = new Node(nx, ny, g, h, current);
					allNodes.put(k, next);
					open.add(next);
				}
			}
		}

		return Collections.emptyList();
	}

	private static double heuristic(Cell a, Cell b) {
		return (double) Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
	}

	private String key(int x, int y) {
		return x + "," + y;
	}

	private static List<Cell> reconstruct(Node node) {
		List<Cell> path = new ArrayList<>();
		Node current = node;
		while (current != null) {
			path.add(new Cell(current.x, current.y));
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}

	private static class Node {
		final int x;
		final int y;
		final double g;
		final double h;
		final double f;
		final Node parent;

		Node(int x, int y, double g, double h, Node parent) {
			this.x = x;
			this.y = y;
			this.g = g;
			this.h = h;
			this.f = g + h;
			this.parent = parent;
		}
	}
}
