package com.example.Board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Corps de POST /api/boards?ownerId=...
 *
 * Seul le titre est accepte. L'id, createdAt, owner / ownerId et tasks sont
 * imposes par le serveur ; meme si le client les envoie, ils sont ignores.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardRequest {

    @NotBlank
    private String title;
}
