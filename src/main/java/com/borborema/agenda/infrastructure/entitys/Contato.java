package com.borborema.agenda.infrastructure.entitys;

import jakarta.persistence.*;
import lombok.*;
import lombok.Data;



@Getter
@Setter
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


}
