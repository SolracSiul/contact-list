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
    public ResponseEntity<Void> salvarContato(@RequestBody ContatoDTO contato) throws NoSuchAlgorithmException, InvalidKeySpecException {
        contatoService.salvarContato(contato);
        return ResponseEntity.ok().build();
    }

    @GetMapping("list/user/contacts")
    public ResponseEntity<List<ContatoDAO>> findUserContacts(@RequestParam UUID userId, @RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<Contato> contatos = contatoService.findContactsByUserId(userId);

        List<ContatoDAO> contactsDAO = Contato.contactListToDAOList(contatos,stringPrivateKey );

        return ResponseEntity.ok(contactsDAO);

    }

    @GetMapping("list/user/contact")
    public ResponseEntity<ContatoDAO> buscarContatoPeloNumero(@RequestParam Long numero, @RequestParam UUID userId, @RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Contato contato = contatoService.buscarContatoUsuarioPorNumero(numero,userId, stringPrivateKey);
        ContatoDAO contatoDAO = Contato.toDAO(contato,stringPrivateKey);
        return ResponseEntity.ok(contatoDAO);
    }

    @DeleteMapping
    public ResponseEntity deleterContatoPeloNumero(@RequestParam Long numero, @RequestParam UUID userId){
        //TODO preciso descriptografar e fazer a deleção
        contatoService.deletarUsuarioContatoPorNumero(numero, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> atualizarContatoPeloNumero(@RequestParam Long numero, @RequestBody ContatoDTO contatoDTO,@RequestParam String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //TODO ADICIONAR O USERID no att

        contatoService.atualizarContatoPorNumero(numero, contatoDTO,stringPrivateKey);
        return ResponseEntity.ok().build();
    }
    //TODO VOLTAR O COLE SEU ID

}
