package com.borborema.agenda.integration;

import com.borborema.agenda.business.UserService;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.entitys.user.UserRole;
import com.borborema.agenda.infrastructure.models.UserAuthenticationDTO;
import com.borborema.agenda.infrastructure.models.UserRegisterDTO;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTES DE INTEGRAÇÃO - UserService
 *
 * Testa a integração entre UserService, UserRepository e CriptoService
 * Valida que os componentes funcionam corretamente em conjunto
 */
@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Limpar base de dados antes de cada teste
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Limpar base de dados após cada teste
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser_DeveGerarChavePrivada() {
        UserRegisterDTO registerDTO = new UserRegisterDTO("integration@test.com", "senha123", UserRole.USER);

        String privateKey = userService.createUser(registerDTO);

        assertNotNull(privateKey, "Chave privada não deve ser nula");
        assertTrue(privateKey.length() > 0, "Chave privada deve ter conteúdo");

        // Verificar que usuário foi salvo no banco
        User savedUser = (User) userRepository.findByEmail("integration@test.com");
        assertNotNull(savedUser, "Usuário deve estar salvo no banco");
        assertNotNull(savedUser.getPublicKey(), "Usuário deve ter chave pública");
    }

    @Test
    void testCreateUser_DeveCriptografarSenha() {
        UserRegisterDTO registerDTO = new UserRegisterDTO("password@test.com", "senha123", UserRole.USER);

        userService.createUser(registerDTO);

        User savedUser = (User) userRepository.findByEmail("password@test.com");
        assertNotNull(savedUser);
        assertNotEquals("senha123", savedUser.getPassword(), "Senha deve estar criptografada");
        assertTrue(savedUser.getPassword().startsWith("$2a$"), "Senha deve usar BCrypt");
    }

    @Test
    void testAuthenticate_ComCredenciaisValidas() {
        // Criar usuário primeiro
        UserRegisterDTO registerDTO = new UserRegisterDTO("auth@test.com", "senha123", UserRole.USER);
        userService.createUser(registerDTO);

        // Tentar autenticar
        UserAuthenticationDTO authDTO = new UserAuthenticationDTO("auth@test.com", "senha123");
        String token = userService.authenticate(authDTO);

        assertNotNull(token, "Token JWT não deve ser nulo");
        assertTrue(token.length() > 0, "Token deve ter conteúdo");
    }

    @Test
    void testAuthenticate_ComCredenciaisInvalidas_DeveLancarException() {
        // Criar usuário
        UserRegisterDTO registerDTO = new UserRegisterDTO("wrongpass@test.com", "senha123", UserRole.USER);
        userService.createUser(registerDTO);

        // Tentar autenticar com senha errada
        UserAuthenticationDTO authDTO = new UserAuthenticationDTO("wrongpass@test.com", "senhaErrada");

        assertThrows(Exception.class, () -> {
            userService.authenticate(authDTO);
        }, "Deve lançar exceção para credenciais inválidas");
    }

    @Test
    void testListUsers_DeveRetornarTodosUsuarios() {
        // Criar múltiplos usuários
        userService.createUser(new UserRegisterDTO("user1@test.com", "senha123", UserRole.USER));
        userService.createUser(new UserRegisterDTO("user2@test.com", "senha456", UserRole.ADMIN));
        userService.createUser(new UserRegisterDTO("user3@test.com", "senha789", UserRole.USER));

        List<User> users = userService.listUsers();

        assertNotNull(users);
        assertEquals(3, users.size(), "Deve retornar 3 usuários");
    }

    @Test
    void testListUsers_ComBaseDeDadosVazia() {
        List<User> users = userService.listUsers();

        assertNotNull(users);
        assertEquals(0, users.size(), "Deve retornar lista vazia");
    }

    @Test
    void testIntegracaoCompleta_FluxoRegistroELogin() {
        // 1. Registrar usuário
        UserRegisterDTO registerDTO = new UserRegisterDTO("fullflow@test.com", "senha123", UserRole.USER);
        String privateKey = userService.createUser(registerDTO);

        assertNotNull(privateKey, "Registro deve retornar chave privada");

        // 2. Fazer login
        UserAuthenticationDTO authDTO = new UserAuthenticationDTO("fullflow@test.com", "senha123");
        String token = userService.authenticate(authDTO);

        assertNotNull(token, "Login deve retornar token");

        // 3. Listar usuários e verificar que o novo usuário está na lista
        List<User> users = userService.listUsers();
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("fullflow@test.com")),
                "Usuário criado deve aparecer na listagem");
    }

    @Test
    void testCreateMultipleUsers_DevesGerarChavesRSADiferentes() {
        String privateKey1 = userService.createUser(new UserRegisterDTO("unique1@test.com", "senha123", UserRole.USER));
        String privateKey2 = userService.createUser(new UserRegisterDTO("unique2@test.com", "senha456", UserRole.USER));

        assertNotEquals(privateKey1, privateKey2, "Chaves privadas devem ser diferentes");

        User user1 = (User) userRepository.findByEmail("unique1@test.com");
        User user2 = (User) userRepository.findByEmail("unique2@test.com");

        assertNotEquals(user1.getPublicKey(), user2.getPublicKey(), "Chaves públicas devem ser diferentes");
    }
}