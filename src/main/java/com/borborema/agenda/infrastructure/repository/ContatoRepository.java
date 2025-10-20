package com.borborema.agenda.infrastructure.repository;

import com.borborema.agenda.infrastructure.entitys.Contato;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContatoRepository extends JpaRepository<Contato, Integer> {

    Optional<Contato> findByNumero(Long numero);


    @Transactional
    void deleteByNumero(Long numero);

}
