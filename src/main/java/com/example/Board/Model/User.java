package com.example.Board.model;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entite User : correspond directement a la classe "User" du diagramme
 * de classes.
 *
 * @Entity indique a Hibernate que cette classe doit etre mappee
 * vers une table de la base de donnees (ici : "users").
 */
@Entity
@Table(name = "users") // "user" est un mot reserve dans certaines BD (ex. Postgres) -> on utilise "users"
@Getter
@Setter
@NoArgsConstructor   // requis par JPA/Hibernate (constructeur sans argument)
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String FirstName;

    @Column(nullable = false)
    private String LastName;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Relation 1 (User) -- 0..* (Board), cote "1" de la relation.
     *
     * mappedBy = "owner" : c'est l'entite Board qui possede la colonne
     * de jointure (owner_id). User ne fait que "voir" la relation depuis
     * son cote -> c'est le cote inverse (non proprietaire) de la relation.
     *
     * cascade = ALL + orphanRemoval = true : si on supprime un User,
     * Hibernate supprime aussi tous ses Boards (et, par cascade depuis
     * Board, toutes leurs Tasks).
     */
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();
}
