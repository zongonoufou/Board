package com.example.Board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Corps de POST /api/users — ce que le client a le droit d'envoyer.
 *
 * Il n'y a VOLONTAIREMENT ni id, ni boards : ces champs sont decides par le
 * serveur. C'est la protection contre le "mass assignment" (un client qui
 * enverrait "id": 999 pour ecraser un autre utilisateur).
 *
 * ignoreUnknown = true : un champ supplementaire dans le JSON est ignore
 * silencieusement au lieu de faire echouer la deserialisation. Cela rend le
 * contrat tolerant et explicite, meme hors configuration Spring Boot.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;
}
