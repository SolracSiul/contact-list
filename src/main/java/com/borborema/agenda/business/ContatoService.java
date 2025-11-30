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
import java.security.PrivateKey;
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

        PublicKey publicKey = CriptoService.getPublicKey(user.getPublicKey());

        String numeroToEncrypt = Long.toString(contatoDTO.numero());

        String endereco = criptoService.rsaEncrypt(contatoDTO.endereco(),publicKey);
        String nome = criptoService.rsaEncrypt(contatoDTO.nome(),publicKey);
        String numero = criptoService.rsaEncrypt(numeroToEncrypt, publicKey);

        contato.setNome(nome);
        contato.setNumero(numero);
        contato.setEndereco(endereco);

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
            throw  new RuntimeException("Numero não encontrado");
        }

        return contato;

    }

    public void deletarUsuarioContatoPorNumero(Long numero, UUID userId){
        String stringNumero = Long.toString(numero);
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("Usuario não encontrado"));
        try{

            PublicKey publicKey = CriptoService.getPublicKey(user.getPublicKey());

            String encriptedNumber = criptoService.rsaEncrypt(stringNumero,publicKey);

            crepository.deleteByNumeroAndUser_UserId(encriptedNumber,userId);

        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public void atualizarContatoPorNumero(Long numero, ContatoDTO contatoDTO, String stringPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //TODO criptografar o dado na entrada

        Contato contatoBuscadoEntity = buscarContatoUsuarioPorNumero(numero, contatoDTO.userId(), stringPrivateKey);

        User user = contatoBuscadoEntity.getUser();

        PublicKey publicKey = CriptoService.getPublicKey(user.getPublicKey());

        String numeroToEncrypt = Long.toString(contatoDTO.numero());

        Contato contatoAtualizado = Contato.builder()
                .nome(contatoDTO.nome() != null ? criptoService.rsaEncrypt(contatoDTO.nome(),publicKey) : contatoBuscadoEntity.getNome())
                .numero(contatoDTO.numero() < 1 ?  criptoService.rsaEncrypt(numeroToEncrypt, publicKey) : contatoBuscadoEntity.getNumero())
                .endereco(contatoDTO.endereco() != null ? criptoService.rsaEncrypt(contatoDTO.endereco(),publicKey) : contatoBuscadoEntity.getEndereco())
                .id(contatoBuscadoEntity.getId())
                .user(contatoBuscadoEntity.getUser())
                .build();
        crepository.saveAndFlush(contatoAtualizado);
    }


}
