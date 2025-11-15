package com.borborema.agenda.infrastructure.models;

import com.borborema.agenda.infrastructure.entitys.user.UserRole;

public record UserRegisterDTO(String email, String password, UserRole userRole) {
}
