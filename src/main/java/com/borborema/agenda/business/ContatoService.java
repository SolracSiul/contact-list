package com.borborema.agenda.business;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContatoService {

    private final ContatoRepository crepository;


    public ContatoService(ContatoRepository crepository){
        this.crepository = crepository;
    }

    public List<Contato> listarcontatos(){
        return crepository.findAll();
    }


    public void salvarContato(Contato contato){
        crepository.saveAndFlush(contato);
    }

    public Contato buscarContatoPorNumero(Long numero){
        return crepository.findByNumero(numero).orElseThrow(
                () -> new RuntimeException("Numero não encontrado")
        );
    }

    public void deletarContatoPorNumero(Long numero){
        crepository.deleteByNumero(numero);
    }


    public void atualizarContatoPorNumero(Long numero, Contato contato) {
        Contato contatoBuscadoEntity = buscarContatoPorNumero(numero);
        Contato contatoAtualizado = Contato.builder()
                .nome(contato.getNome() != null ? contato.getNome() : contatoBuscadoEntity.getNome())
                .numero(contato.getNumero() != null ? contato.getNumero() : contatoBuscadoEntity.getNumero())
                .id(contatoBuscadoEntity.getId())
                .build();
        crepository.saveAndFlush(contatoAtualizado);
    }

}
