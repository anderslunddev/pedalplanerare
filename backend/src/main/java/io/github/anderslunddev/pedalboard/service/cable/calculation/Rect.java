package io.github.anderslunddev.pedalboard.service.cable.calculation;

record Rect(double x, double y, double width, double height) {// TODO the methods in this class are named in a way where
																// they dont make sense. im not even sure if the class
																// name is great.
	public double right() {
		return x + width;
	}

	public double bottom() {
		return y + height;
	}
}
