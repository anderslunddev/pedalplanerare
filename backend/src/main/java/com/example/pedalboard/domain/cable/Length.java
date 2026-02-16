package com.example.pedalboard.domain.cable;

public record Length(double value) {

	public Length {
		if (value <= 0) {
			throw new IllegalArgumentException("Length must be greater than zero");
		}
	}

	@Override
	public String toString() {
		return "Length{value=" + value + "}";
	}
}
