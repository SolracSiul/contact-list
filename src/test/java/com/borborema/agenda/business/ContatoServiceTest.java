package com.borborema.agenda.business;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class ContatoServiceTest {

    @Mock
    private ContatoRepository contatoRepository;

    @InjectMocks
    private ContatoService contatoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSalvarContato() {
        Contato contato = Contato.builder()
                .nome("João Silva")
                .numero(123456789L)
                .build();

        when(contatoRepository.saveAndFlush(any(Contato.class))).thenReturn(contato);

        contatoService.salvarContato(contato);

        verify(contatoRepository, times(1)).saveAndFlush(contato);
    }

    @Test
    void testListarContatos() {
        List<Contato> contatos = Arrays.asList(
                Contato.builder().nome("João").numero(111111111L).build(),
                Contato.builder().nome("Maria").numero(222222222L).build()
        );

        when(contatoRepository.findAll()).thenReturn(contatos);

        List<Contato> resultado = contatoService.listarcontatos();

        assertEquals(2, resultado.size());
        assertEquals("João", resultado.get(0).getNome());
        assertEquals("Maria", resultado.get(1).getNome());
    }

    @Test
    void testBuscarContatoPorNumero_Sucesso() {
        Long numero = 123456789L;
        Contato contato = Contato.builder()
                .nome("João Silva")
                .numero(numero)
                .build();

        when(contatoRepository.findByNumero(numero)).thenReturn(Optional.of(contato));

        Contato resultado = contatoService.buscarContatoPorNumero(numero);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals(numero, resultado.getNumero());
    }

    @Test
    void testBuscarContatoPorNumero_NaoEncontrado() {
        Long numero = 999999999L;
        when(contatoRepository.findByNumero(numero)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contatoService.buscarContatoPorNumero(numero);
        });

        assertEquals("Numero não encontrado", exception.getMessage());
    }

    @Test
    void testDeletarContatoPorNumero() {
        Long numero = 123456789L;
        doNothing().when(contatoRepository).deleteByNumero(numero);

        contatoService.deletarContatoPorNumero(numero);

        verify(contatoRepository, times(1)).deleteByNumero(numero);
    }

    @Test
    void testAtualizarContatoPorNumero_AtualizaNomeENome() {
        Long numero = 123456789L;
        Contato contatoExistente = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero(numero)
                .build();

        Contato novosDados = Contato.builder()
                .nome("João Pedro Silva")
                .numero(987654321L)
                .build();

        when(contatoRepository.findByNumero(numero)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.saveAndFlush(any(Contato.class))).thenReturn(contatoExistente);

        contatoService.atualizarContatoPorNumero(numero, novosDados);

        verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
    }

    @Test
    void testAtualizarContatoPorNumero_AtualizaSoNome() {
        Long numero = 123456789L;
        Contato contatoExistente = Contato.builder()
                .id(1)
                .nome("João Silva")
                .numero(numero)
                .build();

        Contato novosDados = Contato.builder()
                .nome("João Pedro Silva")
                .numero(null)
                .build();

        when(contatoRepository.findByNumero(numero)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.saveAndFlush(any(Contato.class))).thenReturn(contatoExistente);

        contatoService.atualizarContatoPorNumero(numero, novosDados);

        verify(contatoRepository, times(1)).saveAndFlush(any(Contato.class));
    }
}