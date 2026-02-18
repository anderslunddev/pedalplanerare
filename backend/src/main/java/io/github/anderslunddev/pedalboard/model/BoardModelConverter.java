package io.github.anderslunddev.pedalboard.model;


import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
 class BoardModelConverter {

    private final PedalModelConverter pedalConverter;

    BoardModelConverter(PedalModelConverter pedalConverter) {
        this.pedalConverter = pedalConverter;
    }

    Board toDomain(BoardModel entity) {
        Objects.requireNonNull(entity, "BoardModel must not be null");
        List<Pedal> domainPedals = entity.getPedals().stream().map(pedalConverter::toDomain).toList();
        BoardName boardName = new BoardName(entity.getName());
        if (entity.getUser() == null || entity.getUser().getId() == null) {
            throw new IllegalStateException("Board " + entity.getId() + " has no associated user");
        }
        UserId userId = new UserId(entity.getUser().getId());
        return new Board(new BoardId(entity.getId()), userId, boardName,
                new SurfaceArea(entity.getWidth(), entity.getHeight()), domainPedals);
    }



	BoardModel toEntity(BoardName name, SurfaceArea surfaceArea, UserModel user) {
        Objects.requireNonNull(name, "BoardName must not be null");
        Objects.requireNonNull(surfaceArea, "SurfaceArea must not be null");
        Objects.requireNonNull(user, "UserModel must not be null");
        return new BoardModel(name.value(), surfaceArea.width(), surfaceArea.height(), user);
    }

}
