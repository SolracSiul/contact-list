package com.borborema.agenda.infrastructure.models;

import com.borborema.agenda.infrastructure.entitys.Contato;

import java.util.List;
import java.util.UUID;

public record UserDTO(UUID userId , String email, String password, List<Contato> contatos) {
}
