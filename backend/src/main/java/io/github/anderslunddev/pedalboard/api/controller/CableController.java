package io.github.anderslunddev.pedalboard.api.controller;

import io.github.anderslunddev.pedalboard.api.dto.CableResponse;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.service.cable.CableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CableController {

	private final CableService cableService;

	public CableController(CableService cableService) {
		this.cableService = cableService;
	}

	@PostMapping("/boards/{boardId}/generate-sequence")
	public ResponseEntity<List<CableResponse>> generateSequence(@PathVariable UUID boardId) {
		var result = cableService.generateSequence(new BoardId(boardId));
		return result.map(cables -> ResponseEntity.ok(cables.stream().map(c -> CableResponse.from(c, boardId)).toList()))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/boards/{boardId}/cables")
	public ResponseEntity<List<CableResponse>> listCables(@PathVariable UUID boardId) {
		return ResponseEntity
				.ok(cableService.listCables(new BoardId(boardId)).stream().map(c -> CableResponse.from(c, boardId))
						.toList());
	}
}
