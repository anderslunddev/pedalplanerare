package com.example.pedalboard.pedal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedals")
@CrossOrigin(origins = "*")
public class PedalController {

    private final PedalRepository pedalRepository;

    public PedalController(PedalRepository pedalRepository) {
        this.pedalRepository = pedalRepository;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedal> updatePedalPosition(@PathVariable UUID id, @RequestBody PedalPositionRequest request) {
        Optional<Pedal> opt = pedalRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Pedal pedal = opt.get();
        if (request.x() != null) {
            pedal.setX(request.x());
        }
        if (request.y() != null) {
            pedal.setY(request.y());
        }
        Pedal saved = pedalRepository.save(pedal);
        return ResponseEntity.ok(saved);
    }

    public record PedalPositionRequest(Double x, Double y) {
    }
}

