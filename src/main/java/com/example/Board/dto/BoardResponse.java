package com.example.Board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Ce que les endpoints /api/boards renvoient : aucune entite JPA n'est exposee. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    private String title;

    @Schema(description = "Date de creation attribuee par le serveur", format = "date-time",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /** Id du proprietaire, sans serializer l'entite User (relation LAZY). */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long ownerId;
}
