package com.borborema.agenda.infrastructure.models;

import java.util.UUID;

public record ContatoDTO(String nome, long numero, UUID userId) {
}
