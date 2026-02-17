package io.github.anderslunddev.pedalboard.model;


import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
 class BoardModelConverter {

   Board toDomain(BoardModel entity) {
        Objects.requireNonNull(entity, "BoardModel must not be null");
        List<Pedal> domainPedals = entity.getPedals().stream().map(this::toDomain).toList();
        BoardName boardName = new BoardName(entity.getName());
        if (entity.getUser() == null || entity.getUser().getId() == null) {
            throw new IllegalStateException("Board " + entity.getId() + " has no associated user");
        }
        UserId userId = new UserId(entity.getUser().getId());
        return new Board(new BoardId(entity.getId()), userId, boardName,
                new SurfaceArea(entity.getWidth(), entity.getHeight()), domainPedals);
    }

     Pedal toDomain(PedalModel entity) {
        Objects.requireNonNull(entity, "PedalModel must not be null");
        Color color = new Color(entity.getColor());
        if (entity.getPlacement() == null || entity.getPlacement() <= 0) {
            throw new IllegalStateException(
                    "Pedal " + entity.getId() + " has invalid placement: " + entity.getPlacement());
        }
        Placement placement = new Placement(entity.getPlacement());
        return new Pedal(new PedalId(entity.getId()), entity.getBoard() != null ? entity.getBoard().getId() : null,
                new PedalName(entity.getName()), new SurfaceArea(entity.getWidth(), entity.getHeight()), color,
                new Coordinate(entity.getX(), entity.getY()), placement);
    }

}
