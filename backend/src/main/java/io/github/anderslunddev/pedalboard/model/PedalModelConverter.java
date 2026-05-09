package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.pedal.*;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
 class PedalModelConverter {

    Pedal toDomain(PedalModel entity) {
        Objects.requireNonNull(entity, "PedalModel must not be null");
        Color color = new Color(entity.getColor());
        if (entity.getPlacement() == null || entity.getPlacement() <= 0) {
            throw new IllegalStateException(
                    "Pedal " + entity.getId() + " has invalid placement: " + entity.getPlacement());
        }
        Placement placement = new Placement(entity.getPlacement());
        return new Pedal(new PedalId(entity.getId()), new PedalName(entity.getName()),
                new SurfaceArea(entity.getWidth(), entity.getHeight()), color,
                new Coordinate(entity.getX(), entity.getY()), placement);
    }

    PedalModel toEntity(PedalToCreate pedalToCreate, Placement placement, BoardModel board) {
        Objects.requireNonNull(pedalToCreate, "PedalToCreate must not be null");
        Objects.requireNonNull(placement, "Placement must not be null");
        Objects.requireNonNull(board, "BoardModel must not be null");
        PedalModel pedal = new PedalModel();
        pedal.setBoard(board);
        pedal.setName(pedalToCreate.getName().value());
        pedal.setWidth(pedalToCreate.getSurfaceArea().width());
        pedal.setHeight(pedalToCreate.getSurfaceArea().height());
        pedal.setColor(pedalToCreate.getColor().value());
        pedal.setX(pedalToCreate.getCoordinate().x());
        pedal.setY(pedalToCreate.getCoordinate().y());
        pedal.setPlacement(placement.value());
        return pedal;
    }
}
