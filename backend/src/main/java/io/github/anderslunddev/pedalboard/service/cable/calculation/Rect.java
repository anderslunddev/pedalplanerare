package io.github.anderslunddev.pedalboard.service.cable.calculation;

record Rect(double x, double y, double width, double height) {

	public double maxX() {
		return x + width;
	}

	public double maxY() {
		return y + height;
	}
}
