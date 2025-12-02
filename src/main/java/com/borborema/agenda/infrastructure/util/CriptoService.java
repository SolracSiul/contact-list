package com.borborema.agenda.infrastructure.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class CriptoService {


    public String cesarEncrypt (String nome, int chave) {
       String lowerName =  nome.toLowerCase();

       String somenteLetras = lowerName.replaceAll("[^A-Za-z]", "");

       StringBuilder tag = new StringBuilder();

       for(char c : somenteLetras.toCharArray()){
           char ch = (char) ((c - 'a' + chave) % 26 + 'a');
           tag.append(ch);
       }

       return tag.toString();
    }

    public String cesarDecript(String text, int shift) {
        return cesarEncrypt(text, 26 - (shift % 26));
    }


    public String rsaEncrypt(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return java.util.Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar", e);
        }
    }

    public static String rsaDecrypt(String encryptedBase64, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decoded = java.util.Base64.getDecoder().decode(encryptedBase64);
            return new String(cipher.doFinal(decoded));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar", e);
        }
    }

    public  KeyPair generateRSAKeyPar() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static PublicKey getPublicKey (String stringKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String cleanKey = stringKey.replaceAll("\\s", "+");

        byte[] decoded = Base64.getDecoder().decode(cleanKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);


        KeyFactory kf = KeyFactory.getInstance("RSA");

        PublicKey publicKey = kf.generatePublic(spec);

        return publicKey;
    }

    public static PrivateKey getPrivateKey(String stringKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String cleanKey = stringKey.replaceAll("\\s", "+");

        byte[] decoded = Base64.getDecoder().decode(cleanKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = kf.generatePrivate(keySpec);

        return privateKey;
    }

}
