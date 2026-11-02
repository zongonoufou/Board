package com.example.Board.dto;

import com.example.Board.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ce que les endpoints de taches renvoient. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    @Schema(description = "Identifiant du tableau de rattachement",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long boardId;
}
