package com.example.Board.model;

/**
 * Statuts possibles d'une tache (valeurs fermees du diagramme de classes).
 *
 * Un enum evite les fautes de frappe et rend les valeurs autorisees
 * explicites. Les constantes sont stockees en base sous forme de chaine
 * ({@code @Enumerated(EnumType.STRING)}) : ajouter une valeur plus tard
 * ne decale pas le sens des donnees deja enregistrees.
 *
 * Attention : ces noms font partie du contrat de l'API REST (JSON).
 * Ils sont aussi utilises par {@code Task} et {@code TaskService}.
 */
public enum TaskStatus {
    A_FAIRE,
    EN_COURS,
    TERMINE
}
