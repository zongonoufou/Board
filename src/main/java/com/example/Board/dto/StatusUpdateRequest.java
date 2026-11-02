package com.example.Board.dto;

import com.example.Board.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Corps de PATCH /api/boards/{boardId}/tasks/{taskId}/status. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusUpdateRequest {

    @NotNull(message = "Le statut est obligatoire")
    private TaskStatus status;
}
