package com.example.Board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entite Board : porte les deux relations du modele.
 * - cote "N" de User (1) -- (0..*) Board  -> champ {@code owner}
 * - cote "1" de Board (1) -- (0..*) Task -> champ {@code tasks}
 */
@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    /** Date de creation imposee par le serveur, jamais par le client. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Cote proprietaire de la relation User -- Board : la table boards
     * porte la colonne owner_id.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Cote inverse de la relation Board -- Task (Task porte board_id).
     * cascade = ALL + orphanRemoval : supprimer un tableau supprime ses taches.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Expose l'identifiant du proprietaire sans serialiser l'entite User
     * (ni declencher le chargement d'un proxy Hibernate lazy).
     */
    @JsonProperty(value = "ownerId", access = JsonProperty.Access.READ_ONLY)
    public Long getOwnerId() {
        return owner == null ? null : owner.getId();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** Ajoute une tache et synchronise les deux cotes de la relation. */
    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);
        task.setBoard(this);
    }

    /** Retire une tache ; orphanRemoval la supprimera en base. */
    public void removeTask(Task task) {
        if (tasks != null) {
            tasks.remove(task);
        }
    }
}
