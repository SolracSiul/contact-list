package com.borborema.agenda.controller;

import com.borborema.agenda.business.ContatoService;
import com.borborema.agenda.infrastructure.entitys.Contato;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class ContatoController {

    private final ContatoService contatoService;

    @PostMapping
    public ResponseEntity<Void> salvarContato(@RequestBody Contato contato){
        contatoService.salvarContato(contato);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Contato> buscarContatoPeloNumero(@RequestParam Long numero){
        return ResponseEntity.ok(contatoService.buscarContatoPorNumero(numero));
    }

    @DeleteMapping
    public ResponseEntity deleterContatoPeloNumero(@RequestParam Long numero){
        contatoService.deletarContatoPorNumero(numero);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> atualizarContatoPeloNumero(@RequestParam Long numero, @RequestBody Contato contato){
        contatoService.atualizarContatoPorNumero(numero, contato);
        return ResponseEntity.ok().build();
    }


}
