package com.borborema.agenda.infrastructure.entitys;

import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.ContatoDAO;
import com.borborema.agenda.infrastructure.util.CriptoService;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.PrivateKey;
import java.util.ArrayList;
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


    @Column(name = "nome", unique = true)
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

    public static List<ContatoDAO> contactToDAO (List<Contato> contatos, PrivateKey privateKey) {



        List<ContatoDAO> contactsDAO = new ArrayList<>();
        contatos.forEach(contato -> {
            String endereco;
            String nome = contato.nome;
            Long numero = contato.numero;

            try {
                endereco = CriptoService.rsaDecrypt(contato.endereco, privateKey);
            } catch (Exception ex){
                 nome = "#######";
                 numero = 000000l;
                 endereco = contato.endereco;
            }

            ContatoDAO contatoDAO = new ContatoDAO(nome ,numero,endereco) ;
            contactsDAO.add(contatoDAO);
           }
        );

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
