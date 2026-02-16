package com.example.pedalboard.domain.value;

public record SurfaceArea(Double width, Double height) {
	public SurfaceArea {
		if (width == null || width <= 0 || height == null || height <= 0) {
			throw new IllegalArgumentException("Surface area dimensions must be greater than zero");
		}
	}
}
