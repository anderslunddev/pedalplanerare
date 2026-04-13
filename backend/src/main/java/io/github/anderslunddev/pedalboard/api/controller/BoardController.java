package io.github.anderslunddev.pedalboard.api.controller;

import io.github.anderslunddev.pedalboard.api.dto.BoardResponse;
import io.github.anderslunddev.pedalboard.api.dto.PedalResponse;
import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.service.board.BoardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

	private final BoardService boardService;

	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	@PostMapping
	public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody CreateBoardRequest request) {
		Board created = boardService.createBoard(new BoardName(request.name()),
				new SurfaceArea(request.width(), request.height()), new UserId(request.userId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(BoardResponse.from(created));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BoardSummary>> getBoardsForUser(@PathVariable java.util.UUID userId) {
		var boards = boardService.getBoardsForUser(new UserId(userId));
		var summaries = boards.stream().map(b -> new BoardSummary(b.id().value(), b.name().value())).toList();
		return ResponseEntity.ok(summaries);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BoardResponse> getBoard(@PathVariable UUID id) {
		return boardService.getBoard(new BoardId(id)).map(BoardResponse::from).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBoard(@PathVariable UUID id) {
		boardService.deleteBoard(new BoardId(id));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{boardId}/pedals")
	public ResponseEntity<PedalResponse> addPedalToBoard(@PathVariable UUID boardId,
			@Valid @RequestBody CreatePedalRequest request) {

		PedalToCreate.Builder builder = PedalToCreate.builder().name(new PedalName(request.name()))
				.surfaceArea(new SurfaceArea(request.width(), request.height())).color(new Color(request.color()))
				.coordinate(new Coordinate(request.x(), request.y()));
		if (request.placement() != null) {
			builder.placement(new Placement(request.placement()));
		}

		return boardService.addPedalToBoard(new BoardId(boardId), builder.build()).map(created -> ResponseEntity
				.created(java.net.URI.create("/api/pedals/" + created.id().value())).body(PedalResponse.from(created)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	public record CreateBoardRequest(@NotBlank(message = "Board name must not be blank") String name,
			@Positive(message = "Board width must be greater than zero") double width,
			@Positive(message = "Board height must be greater than zero") double height,
			@NotNull(message = "User ID must not be null") java.util.UUID userId) {
	}

	public record BoardSummary(UUID id, String name) {
	}

	public record CreatePedalRequest(@NotBlank(message = "Pedal name must not be blank") String name,
			@Positive(message = "Pedal width must be greater than zero") double width,
			@Positive(message = "Pedal height must be greater than zero") double height,
			@NotBlank(message = "Color must not be blank") String color,
			@Min(value = 0, message = "X coordinate must be non-negative") double x,
			@Min(value = 0, message = "Y coordinate must be non-negative") double y,
			@Positive(message = "Placement must be greater than zero") Integer placement // Optional
																														// -
																														// if
																														// null,
																														// domain
																														// will
																														// assign
																														// next
																														// available
																														// placement
	) {
	}
}
