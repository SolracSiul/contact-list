package com.borborema.agenda.business;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import com.borborema.agenda.infrastructure.util.CriptoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
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

        PublicKey publicKey = CriptoService.getPublicKey(user.getPublicKey());

        String numeroToEncrypt = Long.toString(contatoDTO.numero());

        String email = criptoService.rsaEncrypt(contatoDTO.email(),publicKey);
        String nome = criptoService.rsaEncrypt(contatoDTO.nome(),publicKey);
        String numero = criptoService.rsaEncrypt(numeroToEncrypt, publicKey);

        contato.setNome(nome);
        contato.setNumero(numero);
        contato.setEmail(email);
        contato.setModiefiedDate(new Date());

        String tag = criptoService.cesarEncrypt(contatoDTO.nome(), chave);

        contato.setTag(tag);

        crepository.saveAndFlush(contato);
    }

    public Contato buscarContatoUsuarioPorNumero(Long numero, UUID userId, String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

        String stringNumero = Long.toString(numero);

        PrivateKey privateKey = CriptoService.getPrivateKey(stringPrivateKey);

        List<Contato> contatos = user.getContatos();

        Contato contato = new Contato();

        for(int i = 0; i < contatos.size(); i ++){
            Contato c = contatos.get(i);
            String number = c.getNumero();

            String decryptedNumber = CriptoService.rsaDecrypt(number,privateKey);

            if(decryptedNumber.equals(stringNumero)){
                contato = c;
                break;
            }
        }

        if(contato == null){
            throw  new RuntimeException("Contato não encontrado");
        }

        return contato;

    }

    @Transactional
    public void deletarUsuarioContatoPorNumero(Long numero, UUID userId, String stringPrivateKey){
           try {
              Contato contato = this.buscarContatoUsuarioPorNumero(numero,userId,stringPrivateKey);
              contato.getUser().getContatos().remove(contato);
           } catch (Exception ex){
               System.out.println("Usuario não encontrado");
           }
    }

    public void atualizarContatoPorNumero(Long numero, ContatoDTO contatoDTO, String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Contato contatoBuscadoEntity = buscarContatoUsuarioPorNumero(numero, contatoDTO.userId(), stringPrivateKey);

        User user = contatoBuscadoEntity.getUser();

        PublicKey publicKey = CriptoService.getPublicKey(user.getPublicKey());

        String numeroToEncrypt = Long.toString(contatoDTO.numero());

        Contato contatoAtualizado = Contato.builder()
                .nome(contatoDTO.nome() != null ? criptoService.rsaEncrypt(contatoDTO.nome(),publicKey) : contatoBuscadoEntity.getNome())
                .numero(contatoDTO.numero() < 1 ?  criptoService.rsaEncrypt(numeroToEncrypt, publicKey) : contatoBuscadoEntity.getNumero())
                .email(contatoDTO.email() != null ? criptoService.rsaEncrypt(contatoDTO.email(),publicKey) : contatoBuscadoEntity.getEmail())
                .modiefiedDate(new Date())
                .tag(contatoBuscadoEntity.getTag())
                .id(contatoBuscadoEntity.getId())
                .user(contatoBuscadoEntity.getUser())
                .build();
        crepository.saveAndFlush(contatoAtualizado);
    }


    public Contato buscarContatoUsuarioPeloEmail(String email, UUID userId, String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario não encontrado"));


        PrivateKey privateKey = CriptoService.getPrivateKey(stringPrivateKey);

        List<Contato> contatos = user.getContatos();

        Contato contato = new Contato();

        for(int i = 0; i < contatos.size(); i ++){
            Contato c = contatos.get(i);
            String iterationEmail = c.getEmail();

            String decryptedNumber = CriptoService.rsaDecrypt(iterationEmail,privateKey);

            if(decryptedNumber.equals(email)){
                contato = c;
                break;
            }
        }

        if(contato == null){
            throw  new RuntimeException("Contato não encontrado");
        }

        return contato;
    }
}
