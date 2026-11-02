package com.example.Board.model;

/**
 * Statuts possibles d'une tache.
 *
 * Correspond directement a la classe «enumeration» TaskStatus
 * du diagramme de classes : une valeur fermee, pas de texte libre.
 *
 * Utiliser un enum plutot qu'une simple String evite les fautes de frappe
 * ("EN_COUR" au lieu de "EN_COURS") et rend les valeurs autorisees
 * explicites directement dans le code.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}