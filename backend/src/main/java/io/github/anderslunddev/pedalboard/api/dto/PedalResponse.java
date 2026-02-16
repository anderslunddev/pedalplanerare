package io.github.anderslunddev.pedalboard.api.dto;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;

import java.util.UUID;

/**
 * API response DTO for a pedal. Uses primitives for JSON (e.g. placement as
 * number).
 */
public record PedalResponse(UUID id, UUID boardId, String name, double width, double height, String color, double x,
		double y, int placement) {
	public static PedalResponse from(Pedal pedal) {
		return new PedalResponse(pedal.id().value(), pedal.boardId(), pedal.name().value(), pedal.surfaceArea().width(),
				pedal.surfaceArea().height(), pedal.color().value(), pedal.coordinate().x(), pedal.coordinate().y(),
				pedal.placement().value());
	}
}
