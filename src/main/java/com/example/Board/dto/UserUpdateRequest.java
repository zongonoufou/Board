package com.example.Board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Corps de PUT /api/users/{id} — mise a jour PARTIELLE.
 *
 * Difference essentielle avec UserRequest : ici tout est optionnel, car un
 * champ absent signifie "ne pas modifier". On n'utilise donc PAS @NotBlank
 * (qui interdirait null) : @Size(min = 1) accepte null mais refuse "", et
 * le service verifie egalement les espaces seuls.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdateRequest {

    @Size(min = 1)
    private String firstName;

    @Size(min = 1)
    private String lastName;

    @Email
    @Size(min = 1)
    private String email;
}
