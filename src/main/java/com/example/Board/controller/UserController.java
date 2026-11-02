package com.example.Board.controller;

import com.example.Board.dto.UserRequest;
import com.example.Board.dto.UserResponse;
import com.example.Board.dto.UserUpdateRequest;
import com.example.Board.mapper.UserMapper;
import com.example.Board.model.User;
import com.example.Board.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Couche Controller : expose l'API REST pour les utilisateurs.
 *
 * Frontiere HTTP : on recoit des DTO de REQUETE, on les traduit en entites
 * avec UserMapper, on delegue au Service (qui ne connait que les entites),
 * puis on traduit le resultat en DTO de REPONSE. Aucune entite JPA ne sort
 * d'ici : le contrat JSON est independant du modele de persistance.
 *
 * POST   /api/users       -> creer un utilisateur
 * GET    /api/users       -> lister tous les utilisateurs
 * GET    /api/users/{id}  -> recuperer un utilisateur par id
 * PUT    /api/users/{id}  -> modifier un utilisateur (partiel)
 * DELETE /api/users/{id}  -> supprimer un utilisateur
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User created = userService.createUser(UserMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(UserMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long id,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        User updated = userService.updateUser(id, UserMapper.toEntity(request));
        return ResponseEntity.ok(UserMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
