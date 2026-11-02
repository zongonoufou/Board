package com.example.Board.dto;

import com.example.Board.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Corps de PUT /api/boards/{boardId}/tasks/{taskId} — mise a jour partielle.
 *
 * Contrairement a TaskRequest, le statut EST modifiable ici (la regle RG5 ne
 * concerne que la creation). boardId n'apparait pas : une tache ne peut pas
 * etre deplacee vers un autre tableau par ce biais.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskUpdateRequest {

    @Size(min = 1)
    private String title;

    @Size(max = 1000)
    private String description;

    private TaskStatus status;
}
