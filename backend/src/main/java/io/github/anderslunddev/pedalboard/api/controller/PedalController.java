package io.github.anderslunddev.pedalboard.api.controller;

import io.github.anderslunddev.pedalboard.api.dto.PedalResponse;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.service.pedal.PedalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedals")
public class PedalController {

	private final PedalService pedalService;

	public PedalController(PedalService pedalService) {
		this.pedalService = pedalService;
	}

	@PutMapping("/{id}")
	public ResponseEntity<PedalResponse> updatePedalPosition(@PathVariable UUID id,
			@Valid @RequestBody PedalPositionRequest request) {
		Coordinate coordinate = new Coordinate(request.x(), request.y());
		return pedalService.updatePedalPosition(new PedalId(id), coordinate).map(PedalResponse::from)
				.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePedal(@PathVariable UUID id) {
		boolean deleted = pedalService.deletePedal(new PedalId(id));
		if (!deleted) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	public record PedalPositionRequest(@Min(value = 0, message = "X coordinate must be non-negative") Double x,
			@Min(value = 0, message = "Y coordinate must be non-negative") Double y) {
	}
}
