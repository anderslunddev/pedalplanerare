package com.example.pedalboard.board;

import com.example.pedalboard.pedal.Pedal;
import com.example.pedalboard.pedal.PedalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardRepository boardRepository;
    private final PedalRepository pedalRepository;

    public BoardController(BoardRepository boardRepository, PedalRepository pedalRepository) {
        this.boardRepository = boardRepository;
        this.pedalRepository = pedalRepository;
    }

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board request) {
        if (request.getName() == null || request.getName().isBlank()) {
            request.setName("My Board");
        }
        Board saved = boardRepository.save(request);
        return ResponseEntity.created(URI.create("/api/boards/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoard(@PathVariable UUID id) {
        Optional<Board> board = boardRepository.findById(id);
        return board.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{boardId}/pedals")
    public ResponseEntity<Pedal> addPedalToBoard(@PathVariable UUID boardId, @RequestBody Pedal request) {
        Optional<Board> optBoard = boardRepository.findById(boardId);
        if (optBoard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Board board = optBoard.get();
        request.setBoard(board);
        Pedal saved = pedalRepository.save(request);
        return ResponseEntity.created(URI.create("/api/pedals/" + saved.getId())).body(saved);
    }
}

