package com.borborema.agenda.controller;

import com.borborema.agenda.business.ContatoService;
import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.models.ContatoDAO;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class ContatoController {

    @Autowired
    ContatoService contatoService;

    @PostMapping
    public ResponseEntity<Void> salvarContato(@RequestBody ContatoDTO contato){
        contatoService.salvarContato(contato);
        return ResponseEntity.ok().build();
    }

    @GetMapping("list/user/contacts")
    public ResponseEntity<List<ContatoDAO>> findUserContacts(@RequestParam UUID userId) {
        List<Contato> contatos = contatoService.findContactsByUserId(userId);

       List<ContatoDAO> contactsDAO = Contato.contactToDAO(contatos);

        return ResponseEntity.ok(contactsDAO);
    }

    @GetMapping("list/user/contact")
    public ResponseEntity<Contato> buscarContatoPeloNumero(@RequestParam Long numero, @RequestParam UUID userId){
        return ResponseEntity.ok(contatoService.buscarContatoUsuarioPorNumero(numero,userId));
    }

    @DeleteMapping
    public ResponseEntity deleterContatoPeloNumero(@RequestParam Long numero, @RequestParam UUID userId){
        contatoService.deletarUsuarioContatoPorNumero(numero, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> atualizarContatoPeloNumero(@RequestParam Long numero, @RequestBody ContatoDTO contatoDTO){
        contatoService.atualizarContatoPorNumero(numero, contatoDTO);
        return ResponseEntity.ok().build();
    }


}
