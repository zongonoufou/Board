package com.example.Board.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entite Board : correspond a la classe "Board" du diagramme de classes.
 *
 * Porte les deux relations du modele :
 * - cote "N" de la relation User (1) -- (0..*) Board -> champ owner
 * - cote "1" de la relation Board (1) -- (0..*) Task -> champ tasks
 */
@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Cote proprietaire de la relation User -- Board.
     * 
     * @ManyToOne : plusieurs Board peuvent pointer vers le meme User.
     * @JoinColumn cree la colonne "owner_id" dans la table "boards".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Relation 1 (Board) -- 0..* (Task).
     *
     * mappedBy = "board" : c'est l'entite Task qui possede la colonne
     * de jointure (board_id) -> Board est le cote inverse.
     *
     * cascade = ALL + orphanRemoval = true : supprimer un Board supprime
     * automatiquement toutes ses Tasks. La suppression d'un
     * tableau supprime toutes les taches associees.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Methode utilitaire : ajoute une tache et synchronise les deux cotes
     * de la relation.
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.setBoard(this);
    }
}
