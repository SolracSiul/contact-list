package com.borborema.agenda.business;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.ContatoDTO;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import com.borborema.agenda.infrastructure.util.CriptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void salvarContato(ContatoDTO contatoDTO){

        User user = userRepository.findById(contatoDTO.userId()).orElseThrow(() -> new RuntimeException(("Usuario não encontrado")));

        Contato contato = new Contato();

        contato.setUser(user);
        contato.setNome(contatoDTO.nome());
        contato.setNumero(contatoDTO.numero());

        String tag = criptoService.cifraCesar(contato.getNome(), chave);

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
