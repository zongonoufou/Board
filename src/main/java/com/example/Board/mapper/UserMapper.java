package com.example.Board.mapper;

import com.example.Board.dto.UserRequest;
import com.example.Board.dto.UserResponse;
import com.example.Board.dto.UserUpdateRequest;
import com.example.Board.model.User;

/**
 * Traduction explicite DTO &lt;-&gt; entite, a la frontiere HTTP.
 *
 * Pourquoi ecrire ce code a la main plutot que d'utiliser MapStruct ou de la
 * reflection ? Parce qu'on VOIT chaque champ : c'est ici que se decide ce qui
 * entre et ce qui sort de l'API. Une erreur de securite (copier un id fourni
 * par le client) serait visible d'un coup d'oeil.
 *
 * Le service, lui, continue de travailler avec des entites : il porte les
 * regles metier et les relations JPA, et reste reutilisable hors HTTP.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        // id et boards ne sont PAS copies : le serveur en est seul proprietaire.
        return user;
    }

    /**
     * Pour un PUT partiel, on construit une entite "patch" dont les champs
     * absents restent null : UserService sait qu'un null signifie "ne pas modifier".
     */
    public static User toEntity(UserUpdateRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        return user;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }
}
