package com.example.Board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entite Task : cote proprietaire de la relation Board (1) -- (0..*) Task.
 * La table tasks porte la colonne de jointure board_id.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    /**
     * Stocke le NOM de l'enum ("A_FAIRE", "EN_COURS", "TERMINE") et non
     * son index : ajouter une valeur plus tard ne corrompt pas les donnees.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.A_FAIRE;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /** Expose l'id du tableau sans serialiser l'entite Board (proxy lazy). */
    @JsonProperty(value = "boardId", access = JsonProperty.Access.READ_ONLY)
    public Long getBoardId() {
        return board == null ? null : board.getId();
    }

    /** RG5 : une tache creee sans statut explicite demarre a A_FAIRE. */
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = TaskStatus.A_FAIRE;
        }
    }
}
