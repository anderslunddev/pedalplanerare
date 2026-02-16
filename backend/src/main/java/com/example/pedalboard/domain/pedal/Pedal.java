package com.example.pedalboard.domain.pedal;

import com.example.pedalboard.domain.value.Color;
import com.example.pedalboard.domain.value.Coordinate;
import com.example.pedalboard.domain.value.SurfaceArea;

import java.util.Objects;
import java.util.UUID;

public record Pedal(PedalId id, UUID boardId, PedalName name, SurfaceArea surfaceArea, Color color,
		Coordinate coordinate, Placement placement) {

	public Pedal {
		Objects.requireNonNull(id, "Pedal id must not be null");
		Objects.requireNonNull(boardId, "Pedal boardId must not be null");
		Objects.requireNonNull(name, "Pedal name must not be null");
		Objects.requireNonNull(surfaceArea, "Pedal surface area must not be null");
		Objects.requireNonNull(color, "Pedal color must not be null");
		Objects.requireNonNull(coordinate, "Pedal coordinate must not be null");
		Objects.requireNonNull(placement, "Pedal placement must not be null");
	}
}
