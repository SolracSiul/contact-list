package com.borborema.agenda.infrastructure.entitys;

import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.ContatoDAO;
import com.borborema.agenda.infrastructure.util.CriptoService;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "contato")
@Entity
public class Contato {



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Lob
    @Column(name = "nome", unique = true, columnDefinition = "TEXT")
    private String nome;

    @Column(name = "numero", unique = true)
    private Long numero;

    @Column(name = "tag")
    private String tag;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static List<ContatoDAO> contactToDAO (List<Contato> contatos, String stringPrivateKey) {

        List<ContatoDAO> contactsDAO = new ArrayList<>();

        try{

            String cleanKey = stringPrivateKey.replaceAll("\\s", "+");

            byte[] decoded = Base64.getDecoder().decode(cleanKey);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

            KeyFactory kf = KeyFactory.getInstance("RSA");

            PrivateKey privateKey = kf.generatePrivate(keySpec);

            contatos.forEach(contato -> {
                        String nome;
                        String endereco;
                        long numero = contato.numero;

                        endereco = CriptoService.rsaDecrypt(contato.endereco, privateKey);
                        nome = CriptoService.rsaDecrypt(contato.nome, privateKey);

                        ContatoDAO contatoDAO = new ContatoDAO(nome, numero, endereco);
                        contactsDAO.add(contatoDAO);

                }
            );

        } catch (Exception ex){
            contatos.forEach(contato -> {
                  ContatoDAO contatoDAO = new ContatoDAO(contato.nome, 000000l, contato.endereco);
                  contactsDAO.add(contatoDAO);
                }
            );
        }

        return contactsDAO;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
