package com.borborema.agenda.infrastructure.models;

import java.util.Date;

public record ContatoDAO(String nome, String numero, String endereco, Date modifiedDate) {
}
