package com.example.Board.dto;

import com.example.Board.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Corps de la requete de changement de statut d'une tache. */
@Getter
@Setter
@NoArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "Le statut est obligatoire")
    private TaskStatus status;
}
