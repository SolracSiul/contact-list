package com.borborema.agenda.business;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import com.borborema.agenda.infrastructure.util.CriptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ContatoService {

    @Autowired
    ContatoRepository crepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CriptoService criptoService;

    int chave = 3;

    public ContatoService(ContatoRepository crepository){
        this.crepository = crepository;
    }

    public List<Contato> listarcontatos(){
        return crepository.findAll();
    }

    public List<Contato> findContactsByUserId(UUID userId) {
       User usuario = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("Usuario não encontrado"));

       return usuario.getContatos();
    }

    public void salvarContato(ContatoDTO contatoDTO) throws NoSuchAlgorithmException, InvalidKeySpecException {

        User user = userRepository.findById(contatoDTO.userId()).orElseThrow(() -> new RuntimeException(("Usuario não encontrado")));

        Contato contato = new Contato();

        contato.setUser(user);

        String cleanKey = user.getPublicKey().replaceAll("\\s", "+");

        byte[] decoded = Base64.getDecoder().decode(cleanKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PublicKey publicKey = kf.generatePublic(spec);

        String endereco = criptoService.rsaEncrypt(contatoDTO.endereco(),publicKey);
        String nome = criptoService.rsaEncrypt(contatoDTO.nome(),publicKey);

        contato.setNome(nome);
        contato.setNumero(contatoDTO.numero());
        contato.setEndereco(endereco);

        String tag = criptoService.cesarEncrypt(contatoDTO.nome(), chave);

        contato.setTag(tag);

        crepository.saveAndFlush(contato);
    }

    public Contato buscarContatoUsuarioPorNumero(Long numero, UUID userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

        List<Contato> contatos = user.getContatos();

        Contato contato = new Contato();

        for(int i = 0; i < contatos.size(); i ++){
            Contato c = contatos.get(i);
            if(c.getNumero().equals(numero)){
                contato = c;
                break;
            }
        }

        if(contato == null){
            throw  new RuntimeException("Numero não encontrado");
        }

        return contato;

    }

    public void deletarUsuarioContatoPorNumero(Long numero, UUID userId){
        crepository.deleteByNumeroAndUser_UserId(numero,userId);
    }

    public void atualizarContatoPorNumero(Long numero, ContatoDTO contatoDTO) {
        Contato contatoBuscadoEntity = buscarContatoUsuarioPorNumero(numero, contatoDTO.userId());
        Contato contatoAtualizado = Contato.builder()
                .nome(contatoDTO.nome() != null ? contatoDTO.nome() : contatoBuscadoEntity.getNome())
                .numero(contatoDTO.numero() < 1 ? contatoDTO.numero() : contatoBuscadoEntity.getNumero())
                .id(contatoBuscadoEntity.getId())
                .user(contatoBuscadoEntity.getUser())
                .build();
        crepository.saveAndFlush(contatoAtualizado);
    }


}
