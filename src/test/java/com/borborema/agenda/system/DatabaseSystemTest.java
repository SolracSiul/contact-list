package com.borborema.agenda.system;

import com.borborema.agenda.infrastructure.entitys.Contato;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.entitys.user.UserRole;
import com.borborema.agenda.infrastructure.repository.ContatoRepository;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTES DE SISTEMA AUTOMATIZADOS
 *
 * Simula fluxos completos end-to-end do sistema
 * Testa a aplicação inteira funcionando em conjunto (banco de dados, serviços, etc)
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseSystemTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContatoRepository contatoRepository;

    private static User usuarioTeste;

    @BeforeAll
    static void setupOnce(@Autowired UserRepository userRepo) {
        // Cria usuário uma vez para todos os testes
        usuarioTeste = User.builder()
                .email("sistema@test.com")
                .password("senha123")
                .role(UserRole.USER)
                .build();
        usuarioTeste = userRepo.save(usuarioTeste);
    }

    @AfterAll
    static void cleanupAll(@Autowired ContatoRepository contatoRepo, @Autowired UserRepository userRepo) {
        contatoRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @Order(1)
    void test01_FluxoCompleto_VerificarUsuario() {
        // Verifica que o usuário do sistema foi criado
        assertNotNull(usuarioTeste);
        assertNotNull(usuarioTeste.getUserId());
        assertEquals("sistema@test.com", usuarioTeste.getEmail());
        System.out.println("✓ Fluxo 1: Usuário validado no sistema");
    }

    @Test
    @Order(2)
    void test02_FluxoCompleto_CriarContatos() {
        // Simula usuário criando múltiplos contatos
        Contato contato1 = Contato.builder()
                .nome("Maria Silva")
                .numero("111222333")
                .email("maria@test.com")
                .tag("pduldiloyd")
                .user(usuarioTeste)
                .build();

        Contato contato2 = Contato.builder()
                .nome("João Pedro")
                .numero("444555666")
                .email("joao@test.com")
                .tag("mrrishgur")
                .user(usuarioTeste)
                .build();

        contatoRepository.save(contato1);
        contatoRepository.save(contato2);

        var contatos = contatoRepository.findAll();
        assertTrue(contatos.size() >= 2);
        System.out.println("✓ Fluxo 2: Contatos criados no sistema");
    }

    @Test
    @Order(3)
    void test03_FluxoCompleto_ListarContatos() {
        // Simula usuário listando seus contatos
        var contatos = contatoRepository.findAll();

        assertFalse(contatos.isEmpty());
        assertTrue(contatos.stream().anyMatch(c -> c.getNome().equals("Maria Silva")));
        assertTrue(contatos.stream().anyMatch(c -> c.getNome().equals("João Pedro")));
        System.out.println("✓ Fluxo 3: Contatos listados do banco de dados");
    }

    @Test
    @Order(4)
    void test04_FluxoCompleto_BuscarContato() {
        // Simula usuário buscando um contato específico
        var contatoOpt = contatoRepository.findByNumero("111222333");

        assertTrue(contatoOpt.isPresent());
        assertEquals("Maria Silva", contatoOpt.get().getNome());
        System.out.println("✓ Fluxo 4: Contato encontrado no sistema");
    }

    @Test
    @Order(5)
    void test05_FluxoCompleto_AtualizarContato() {
        // Simula usuário atualizando informações de um contato
        var contatoOpt = contatoRepository.findByNumero("111222333");
        assertTrue(contatoOpt.isPresent());

        Contato contato = contatoOpt.get();
        contato.setNome("Maria Silva Santos");
        contato.setNumero("999888777");
        contatoRepository.save(contato);

        var contatoAtualizado = contatoRepository.findByNumero("999888777");
        assertTrue(contatoAtualizado.isPresent());
        assertEquals("Maria Silva Santos", contatoAtualizado.get().getNome());
        System.out.println("✓ Fluxo 5: Contato atualizado no sistema");
    }

    @Test
    @Order(6)
    void test06_FluxoCompleto_DeletarContato() {
        // Simula usuário deletando um contato
        var contatoOpt = contatoRepository.findByNumero("444555666");
        assertTrue(contatoOpt.isPresent());

        contatoRepository.delete(contatoOpt.get());

        var contatoDeletado = contatoRepository.findByNumero("444555666");
        assertTrue(contatoDeletado.isEmpty());
        System.out.println("✓ Fluxo 6: Contato deletado do sistema");
    }

    @Test
    @Order(7)
    void test07_FluxoCompleto_VerificarIntegridadeReferencial() {
        // Simula teste de integridade: usuário deve ter contatos vinculados
        var usuarioOpt = userRepository.findById(usuarioTeste.getUserId());
        assertTrue(usuarioOpt.isPresent());

        User usuario = usuarioOpt.get();
        assertNotNull(usuario.getContatos());
        System.out.println("✓ Fluxo 7: Integridade referencial validada");
    }

    @Test
    @Order(8)
    void test08_FluxoCompleto_TesteDePerformance() {
        // Simula criação em massa (teste de performance básico)
        long inicio = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            Contato contato = Contato.builder()
                    .nome("Contato " + i)
                    .numero(String.valueOf(1000000L + i))
                    .email("contato" + i + "@test.com")
                    .tag("tag" + i)
                    .user(usuarioTeste)
                    .build();
            contatoRepository.save(contato);
        }

        long fim = System.currentTimeMillis();
        long duracao = fim - inicio;

        assertTrue(duracao < 5000, "Criação de 50 contatos deve levar menos de 5 segundos");
        System.out.println("✓ Fluxo 8: Performance validada - " + duracao + "ms para 50 contatos");
    }
}
