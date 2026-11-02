package com.example.Board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entite User : correspond a la classe "User" du diagramme de classes.
 *
 * "user" est un mot reserve dans certaines bases (PostgreSQL), d'ou le
 * nom de table {@code users}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor   // requis par JPA/Hibernate
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Cote inverse de la relation User (1) -- (0..*) Board.
     * mappedBy = "owner" : c'est Board qui porte la colonne owner_id.
     * La cascade supprime les tableaux (et leurs taches) avec l'utilisateur.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();

    /** Synchronise les deux cotes de la relation. */
    public void addBoard(Board board) {
        if (boards == null) {
            boards = new ArrayList<>();
        }
        boards.add(board);
        board.setOwner(this);
    }

    /** Retire un tableau ; orphanRemoval le supprimera en base. */
    public void removeBoard(Board board) {
        if (boards != null) {
            boards.remove(board);
        }
    }
}
