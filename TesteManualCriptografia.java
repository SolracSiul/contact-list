import com.borborema.agenda.infrastructure.util.CriptoService;

import java.security.KeyPair;

public class TesteManualCriptografia {
    public static void main(String[] args) throws Exception {
        CriptoService criptoService = new CriptoService();

        System.out.println("=== TESTE MANUAL DE CRIPTOGRAFIA ===\n");

        // 1. CIFRA DE CÉSAR
        System.out.println("1. CIFRA DE CÉSAR");
        System.out.println("-".repeat(50));

        String nomeOriginal = "João Silva";
        int chave = 3;

        String nomeEncriptado = criptoService.cesarEncrypt(nomeOriginal, chave);
        String nomeDecriptado = criptoService.cesarDecript(nomeEncriptado, chave);

        System.out.println("Nome Original:     " + nomeOriginal);
        System.out.println("Nome Encriptado:   " + nomeEncriptado);
        System.out.println("Nome Decriptado:   " + nomeDecriptado);
        System.out.println("Funcionou? " + (nomeDecriptado.equalsIgnoreCase(nomeOriginal.replaceAll("[^a-zA-Z]", ""))));

        // 2. TIPO RSA
        System.out.println("\n2. CRIPTOGRAFIA RSA");
        System.out.println("-".repeat(50));

        // Gera par de chaves
        KeyPair keyPair = criptoService.generateRSAKeyPar();

        String enderecoOriginal = "Rua das Flores, 123 - Apto 45";

        String enderecoEncriptado = criptoService.rsaEncrypt(enderecoOriginal, keyPair.getPublic());
        String enderecoDecriptado = CriptoService.rsaDecrypt(enderecoEncriptado, keyPair.getPrivate());

        System.out.println("Endereço Original:      " + enderecoOriginal);
        System.out.println("Endereço Encriptado:    " + enderecoEncriptado.substring(0, 50) + "...");
        System.out.println("Endereço Decriptado:    " + enderecoDecriptado);
        System.out.println("Funcionou? " + enderecoOriginal.equals(enderecoDecriptado));

        // 3. CHAVES RSA
        System.out.println("\n3. INFORMAÇÕES DAS CHAVES");
        System.out.println("-".repeat(50));

        String publicKeyBase64 = java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyBase64 = java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println("Chave Pública (primeiros 50 chars):  " + publicKeyBase64.substring(0, 50) + "...");
        System.out.println("Chave Privada (primeiros 50 chars):  " + privateKeyBase64.substring(0, 50) + "...");
        System.out.println("Tamanho da Chave Pública:  " + keyPair.getPublic().getEncoded().length + " bytes");
        System.out.println("Tamanho da Chave Privada:  " + keyPair.getPrivate().getEncoded().length + " bytes");

        System.out.println("\n=== TESTES CONCLUÍDOS ===");
    }
}
