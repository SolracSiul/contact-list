package com.borborema.agenda.infrastructure.entitys;

import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.ContatoDAO;
import com.borborema.agenda.infrastructure.util.CriptoService;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
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

    @Lob
    @Column(name = "numero", unique = true, columnDefinition = "TEXT")
    private String numero;

    @Column(name = "tag")
    private String tag;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String email;

    @Column
    private Date modiefiedDate;

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

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static ContatoDAO toDAO (Contato contato, String stringPrivateKey){

        try{
            PrivateKey privateKey = CriptoService.getPrivateKey(stringPrivateKey);
            String email = CriptoService.rsaDecrypt(contato.email, privateKey);
            String nome = CriptoService.rsaDecrypt(contato.nome, privateKey);
            String numero = CriptoService.rsaDecrypt(contato.numero,privateKey);

            ContatoDAO contatoDAO = new ContatoDAO(nome,numero, email, contato.getModiefiedDate());

            return contatoDAO;

        }   catch (Exception e){
            ContatoDAO contatoDAO = new ContatoDAO(contato.nome, contato.numero,contato.email, contato.getModiefiedDate());
            return contatoDAO;
        }
    }

    public static List<ContatoDAO> contactListToDAOList (List<Contato> contatos, String stringPrivateKey) {

        List<ContatoDAO> contactsDAO = new ArrayList<>();

        try{

            PrivateKey privateKey = CriptoService.getPrivateKey(stringPrivateKey);

            contatos.forEach(contato -> {
                        String nome;
                        String email;
                        String numero;

                        email = CriptoService.rsaDecrypt(contato.email, privateKey);
                        nome = CriptoService.rsaDecrypt(contato.nome, privateKey);
                        numero = CriptoService.rsaDecrypt(contato.numero,privateKey);

                        ContatoDAO contatoDAO = new ContatoDAO(nome,numero, email, contato.getModiefiedDate());
                        contactsDAO.add(contatoDAO);

                }
            );

        } catch (Exception ex){
            contatos.forEach(contato -> {
                  ContatoDAO contatoDAO = new ContatoDAO(contato.nome, contato.numero, contato.email,contato.getModiefiedDate());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getModiefiedDate() {
        return modiefiedDate;
    }

    public void setModiefiedDate(Date modiefiedDate) {
        this.modiefiedDate = modiefiedDate;
    }
}
