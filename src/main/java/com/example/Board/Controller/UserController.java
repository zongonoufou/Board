package com.example.Board.controller;

import com.example.Board.model.User;
import com.example.Board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Couche Controller : expose l'API REST pour les utilisateurs.
 *
 * Regles d'or du Controller :
 * - Il ne contient AUCUNE logique metier.
 * - Il recoit la requete HTTP, delege au Service, retourne la reponse HTTP.
 * - C'est tout.
 *
 * Endpoints disponibles :
 *   POST   /api/users       -> creer un utilisateur
 *   GET    /api/users       -> lister tous les utilisateurs
 *   GET    /api/users/{id}  -> recuperer un utilisateur par id
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
