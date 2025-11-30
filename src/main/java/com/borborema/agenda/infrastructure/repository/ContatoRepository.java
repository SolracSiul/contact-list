package com.borborema.agenda.infrastructure.repository;

import com.borborema.agenda.infrastructure.entitys.Contato;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ContatoRepository extends JpaRepository<Contato, Integer> {

     Optional<Contato> findByNumero(String numero);


    @Transactional
    void deleteByNumeroAndUser_UserId(String numero, UUID userid);

}
