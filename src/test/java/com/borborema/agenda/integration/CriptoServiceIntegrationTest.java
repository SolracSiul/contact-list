package com.borborema.agenda.integration;

import com.borborema.agenda.infrastructure.util.CriptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTES DE INTEGRAÇÃO
 *
 * Testa a integração entre componentes do Spring
 * Valida que os beans são injetados corretamente e funcionam em conjunto
 */
@SpringBootTest
class CriptoServiceIntegrationTest {

    @Autowired
    private CriptoService criptoService;

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        keyPair = criptoService.generateRSAKeyPar();
    }

    @Test
    void testCriptoServiceBean_DeveEstarInjetado() {
        assertNotNull(criptoService, "CriptoService deve ser injetado pelo Spring");
    }

    @Test
    void testIntegracaoRSA_EncriptarEDescriptografar() throws Exception {
        String mensagemOriginal = "Teste de integração RSA";

        // Encripta com chave pública
        String encriptado = criptoService.rsaEncrypt(mensagemOriginal, keyPair.getPublic());

        // Descriptografa com chave privada
        String decriptado = CriptoService.rsaDecrypt(encriptado, keyPair.getPrivate());

        assertEquals(mensagemOriginal, decriptado);
    }

    @Test
    void testIntegracaoCesar_EncriptarEDescriptografar() {
        String nome = "teste";
        int chave = 5;

        String encriptado = criptoService.cesarEncrypt(nome, chave);
        String decriptado = criptoService.cesarDecript(encriptado, chave);

        assertEquals(nome, decriptado);
    }

    @Test
    void testIntegracaoCompleta_MultiplasCriptografias() throws Exception {
        // Simula fluxo real: criptografar vários endereços
        String[] endereços = {
            "Rua A, 123",
            "Avenida B, 456",
            "Travessa C, 789"
        };

        for (String endereco : endereços) {
            String encriptado = criptoService.rsaEncrypt(endereco, keyPair.getPublic());
            String decriptado = CriptoService.rsaDecrypt(encriptado, keyPair.getPrivate());

            assertEquals(endereco, decriptado);
        }
    }
}
