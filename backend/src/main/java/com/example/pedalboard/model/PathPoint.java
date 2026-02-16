package com.example.pedalboard.model;

import jakarta.persistence.Embeddable;

@Embeddable
class PathPoint {

	private Double x;
	private Double y;

	public PathPoint() {
	}

	public PathPoint(Double x, Double y) {
		this.x = x;
		this.y = y;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}
}
