package com.example.Board.repository;

import com.example.Board.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour l'entite User.
 *
 * JpaRepository fournit gratuitement save(), findById(), findAll(),
 * deleteById(), existsById()... Les methodes ci-dessous sont des
 * "query methods derivees" : Spring Data genere le SQL a partir du nom.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Sert a autoriser la reutilisation de son propre email lors d'un PUT. */
    boolean existsByEmailAndIdNot(String email, Long id);
}
