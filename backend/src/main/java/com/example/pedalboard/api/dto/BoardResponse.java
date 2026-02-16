package com.example.pedalboard.api.dto;

import com.example.pedalboard.domain.board.Board;

import java.util.List;
import java.util.UUID;

/**
 * API response DTO for a board. Uses primitives and PedalResponse for JSON.
 */
public record BoardResponse(UUID id, UUID userId, String name, double width, double height,
		List<PedalResponse> pedals) {
	public static BoardResponse from(Board board) {
		List<PedalResponse> pedals = board.pedals().stream().map(PedalResponse::from).toList();
		return new BoardResponse(board.id().value(), board.userId().value(), board.name().value(),
				board.surfaceArea().width(), board.surfaceArea().height(), pedals);
	}
}
