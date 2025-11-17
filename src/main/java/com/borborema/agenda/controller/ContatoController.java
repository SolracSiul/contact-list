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
    public ResponseEntity<Void> salvarContato(@RequestBody ContatoDTO contato, @RequestParam String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        contatoService.salvarContato(contato, publicKey);
        return ResponseEntity.ok().build();
    }

    @GetMapping("list/user/contacts")
    public ResponseEntity<List<ContatoDAO>> findUserContacts(@RequestParam UUID userId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<Contato> contatos = contatoService.findContactsByUserId(userId);

       String stringKey =  contatos.get(0).getUser().getPrivateKey();

        byte[] decoded = Base64.getDecoder().decode(stringKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = kf.generatePrivate(keySpec);


        List<ContatoDAO> contactsDAO = Contato.contactToDAO(contatos,privateKey );

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
