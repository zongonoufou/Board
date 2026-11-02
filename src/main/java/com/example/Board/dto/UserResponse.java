package com.example.Board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ce que GET / POST / PUT /api/users renvoient.
 *
 * Aucune relation JPA ici : pas de boards, pas de proxy Hibernate, pas de
 * risque de recursivite JSON. L'id est documente en lecture seule : il est
 * present dans la REPONSE mais absent du corps de REQUETE.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @Schema(description = "Identifiant attribue par le serveur", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    private String firstName;

    private String lastName;

    @Schema(format = "email")
    private String email;
}
