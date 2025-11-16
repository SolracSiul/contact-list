package com.borborema.agenda.infrastructure.util;

import org.springframework.stereotype.Component;

@Component
public class CriptoService {


    public String cifraCesar (String nome, int chave) {
       String lowerName =  nome.toLowerCase();

       String somenteLetras = lowerName.replaceAll("[^A-Za-z]", "");

       StringBuilder tag = new StringBuilder();

       for(char c : somenteLetras.toCharArray()){
           char ch = (char) ((c - 'a' + chave) % 26 + 'a');
           tag.append(ch);
       }

       return tag.toString();
    }
}
