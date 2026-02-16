package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PedalRepository extends JpaRepository<PedalModel, UUID> {
}
