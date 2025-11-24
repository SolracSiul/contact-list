package com.borborema.agenda.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class CriptoServiceTest {

    private CriptoService criptoService;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        criptoService = new CriptoService();
        keyPair = criptoService.generateRSAKeyPar();
    }

    @Test
    void testCesarEncrypt_ComNomeSimples() {
        String nome = "joao";
        int chave = 3;

        String resultado = criptoService.cesarEncrypt(nome, chave);

        // j(9)+3=m(12), o(14)+3=r(17), a(0)+3=d(3), o(14)+3=r(17)
        assertEquals("mrdr", resultado);
    }

    @Test
    void testCesarEncrypt_ComNomeComplexo() {
        String nome = "João Silva";
        int chave = 3;

        String resultado = criptoService.cesarEncrypt(nome, chave);

        // "João Silva" -> "JooSilva" (acento removido) -> "joosilva"
        // j+3=m, o+3=r, o+3=r, s+3=v, i+3=l, l+3=o, v+3=y, a+3=d
        assertEquals("mrrvloyd", resultado);
    }

    @Test
    void testCesarEncrypt_ComCaracteresEspeciais() {
        String nome = "Ana-Paula123";
        int chave = 5;

        String resultado = criptoService.cesarEncrypt(nome, chave);

        // "Ana-Paula123" -> "AnaPaula" -> "anapaula"
        // a+5=f, n+5=s, a+5=f, p+5=u, a+5=f, u+5=z, l+5=q, a+5=f
        assertEquals("fsfufzqf", resultado);
    }

    @Test
    void testCesarDecrypt_DecriptaCesarEncrypt() {
        String nome = "teste";
        int chave = 3;

        String encriptado = criptoService.cesarEncrypt(nome, chave);
        String decriptado = criptoService.cesarDecript(encriptado, chave);

        assertEquals(nome, decriptado);
    }

    @Test
    void testCesarEncryptDecrypt_ComDiferentesChaves() {
        String nome = "maria";

        for (int chave = 1; chave <= 25; chave++) {
            String encriptado = criptoService.cesarEncrypt(nome, chave);
            String decriptado = criptoService.cesarDecript(encriptado, chave);
            assertEquals(nome, decriptado, "Falhou com chave: " + chave);
        }
    }

    @Test
    void testRsaEncrypt_EncriptaTexto() throws Exception {
        String textoOriginal = "Endereco secreto: Rua A, 123";

        String encriptado = criptoService.rsaEncrypt(textoOriginal, keyPair.getPublic());

        assertNotNull(encriptado);
        assertNotEquals(textoOriginal, encriptado);
        assertTrue(encriptado.length() > 0);
    }

    @Test
    void testRsaDecrypt_DecriptaTextoEncriptado() throws Exception {
        String textoOriginal = "Endereco secreto: Rua A, 123";

        String encriptado = criptoService.rsaEncrypt(textoOriginal, keyPair.getPublic());
        String decriptado = CriptoService.rsaDecrypt(encriptado, keyPair.getPrivate());

        assertEquals(textoOriginal, decriptado);
    }

    @Test
    void testRsaEncryptDecrypt_ComDiferentesTextos() throws Exception {
        String[] textos = {
                "Texto curto",
                "Texto com números 123456",
                "Texto com especiais !@#$%",
                "Rua das Flores, 123 - Apto 45"
        };

        for (String texto : textos) {
            String encriptado = criptoService.rsaEncrypt(texto, keyPair.getPublic());
            String decriptado = CriptoService.rsaDecrypt(encriptado, keyPair.getPrivate());
            assertEquals(texto, decriptado, "Falhou com texto: " + texto);
        }
    }

    @Test
    void testRsaEncrypt_ComChaveInvalida_DeveLancarException() {
        String texto = "Teste";

        assertThrows(RuntimeException.class, () -> {
            criptoService.rsaEncrypt(texto, null);
        });
    }

    @Test
    void testRsaDecrypt_ComChaveInvalida_DeveLancarException() {
        assertThrows(RuntimeException.class, () -> {
            CriptoService.rsaDecrypt("textoInvalido", null);
        });
    }

    @Test
    void testGenerateRSAKeyPar_GeraParDeChaves() throws Exception {
        KeyPair novoKeyPair = criptoService.generateRSAKeyPar();

        assertNotNull(novoKeyPair);
        assertNotNull(novoKeyPair.getPublic());
        assertNotNull(novoKeyPair.getPrivate());
        assertEquals("RSA", novoKeyPair.getPublic().getAlgorithm());
        assertEquals("RSA", novoKeyPair.getPrivate().getAlgorithm());
    }

    @Test
    void testGenerateRSAKeyPar_GeraChavesDiferentes() throws Exception {
        KeyPair keyPair1 = criptoService.generateRSAKeyPar();
        KeyPair keyPair2 = criptoService.generateRSAKeyPar();

        // As chaves devem ser diferentes
        assertNotEquals(keyPair1.getPublic(), keyPair2.getPublic());
        assertNotEquals(keyPair1.getPrivate(), keyPair2.getPrivate());
    }

    @Test
    void testRsaEncryptDecrypt_ChavesDiferentesNaoFuncionam() throws Exception {
        String textoOriginal = "Texto secreto";
        KeyPair keyPair1 = criptoService.generateRSAKeyPar();
        KeyPair keyPair2 = criptoService.generateRSAKeyPar();

        String encriptado = criptoService.rsaEncrypt(textoOriginal, keyPair1.getPublic());

        // Tentar decriptar com chave privada diferente deve lançar exceção
        assertThrows(RuntimeException.class, () -> {
            CriptoService.rsaDecrypt(encriptado, keyPair2.getPrivate());
        });
    }
}
