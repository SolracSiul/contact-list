package com.borborema.agenda.controller;

import com.borborema.agenda.business.ContatoService;
import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.models.ContatoDAO;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class ContatoController {

    @Autowired
    ContatoService contatoService;

    @PostMapping
    public ResponseEntity<?> salvarContato(@RequestBody ContatoDTO contato) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            contatoService.salvarContato(contato);
            return ResponseEntity.ok().build();
        } catch (Exception ex){
            return ResponseEntity.badRequest().body("Não foi possível cadastrar o contato");
        }

    }

    @GetMapping("list/user/contacts")
    public ResponseEntity<?> findUserContacts(@RequestParam UUID userId, @RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

       try {
           List<Contato> contatos = contatoService.findContactsByUserId(userId);

           List<ContatoDAO> contactsDAO = Contato.contactListToDAOList(contatos,stringPrivateKey );

           return ResponseEntity.ok(contactsDAO);
       } catch (Exception ex){
           return ResponseEntity.badRequest().body("Não foi possível retornar os contatos");
       }

    }

    @GetMapping("list/user/contact/number")
    public ResponseEntity<?> buscarContatoPeloNumero(@RequestParam Long numero, @RequestParam UUID userId, @RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        try {
            Contato contato = contatoService.buscarContatoUsuarioPorNumero(numero,userId, stringPrivateKey);
            ContatoDAO contatoDAO = Contato.toDAO(contato,stringPrivateKey);
            return ResponseEntity.ok(contatoDAO);
        } catch (Exception ex){
            return ResponseEntity.badRequest().body("Não foi possível encontrar o contato pelo número");
        }

    }

    @GetMapping("list/user/contact/email")
    public ResponseEntity<?> buscarContatoPeloEmail(@RequestParam String email, @RequestParam UUID userId, @RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        try {
            Contato contato = contatoService.buscarContatoUsuarioPeloEmail(email,userId, stringPrivateKey);
            ContatoDAO contatoDAO = Contato.toDAO(contato,stringPrivateKey);
            return ResponseEntity.ok(contatoDAO);
        } catch (Exception ex){
            return ResponseEntity.badRequest().body("Não foi possível encontrar o contato pelo e-mail");
        }

    }

    @DeleteMapping
    public ResponseEntity deleterContatoPeloNumero(@RequestParam Long numero, @RequestParam UUID userId, @RequestParam String stringPrivateKey){

        try {
            contatoService.deletarUsuarioContatoPorNumero(numero, userId, stringPrivateKey);
            return ResponseEntity.ok().build();
        } catch (Exception ex){
            return ResponseEntity.badRequest().body("Não foi possível deletar o contato");
        }

    }

    @PutMapping
    public ResponseEntity<?> atualizarContatoPeloNumero(@RequestParam Long numero, @RequestBody ContatoDTO contatoDTO,@RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            contatoService.atualizarContatoPorNumero(numero, contatoDTO, stringPrivateKey);
            return ResponseEntity.ok().build();
        }
         catch (Exception ex){
            return ResponseEntity.badRequest().body("Não foi possível atualizar o contato");
        }
    }

}
