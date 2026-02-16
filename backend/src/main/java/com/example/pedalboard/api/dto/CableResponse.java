package com.example.pedalboard.api.dto;

import com.example.pedalboard.domain.cable.Cable;

import java.util.List;
import java.util.UUID;

/**
 * API response DTO for a cable. Uses primitives for JSON (e.g. totalLength as
 * number).
 */
public record CableResponse(UUID id, UUID boardId, UUID sourcePedalId, UUID destinationPedalId,
		List<PathPointDto> pathPoints, double totalLength) {
	public record PathPointDto(double x, double y) {
	}

	public static CableResponse from(Cable cable) {
		List<PathPointDto> points = cable.pathPoints().stream().map(p -> new PathPointDto(p.x(), p.y())).toList();
		return new CableResponse(cable.id().value(), cable.boardId().value(), cable.sourcePedalId().value(),
				cable.destinationPedalId().value(), points, cable.totalLength().value());
	}
}
