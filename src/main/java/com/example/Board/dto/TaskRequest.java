package com.example.Board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Corps de POST /api/boards/{boardId}/tasks.
 *
 * Pas de status : la regle metier RG5 impose A_FAIRE a la creation. Un client
 * qui envoie "status":"TERMINE" est donc ignore, et springdoc documente clairement
 * que ce champ n'est pas accepte en entree.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRequest {

    @NotBlank
    private String title;

    @Size(max = 1000)
    private String description;
}
