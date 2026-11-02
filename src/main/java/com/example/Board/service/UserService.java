package com.example.Board.service;

import com.example.Board.exception.DuplicateResourceException;
import com.example.Board.exception.ResourceNotFoundException;
import com.example.Board.model.User;
import com.example.Board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Couche Service : contient la LOGIQUE METIER des utilisateurs.
 *
 * @RequiredArgsConstructor (Lombok) genere le constructeur d'injection
 * de UserRepository. Les acces en lecture sont en transaction read-only ;
 * les methodes d'ecriture sont transactionnelles.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Cree un utilisateur apres avoir verifie l'unicite de l'email.
     * L'email est normalise (espaces + casse) pour fiabiliser la comparaison.
     */
    @Transactional
    public User createUser(User user) {
        String email = normalizeEmail(user.getEmail());
        requireNotBlank(user.getFirstName(), "firstName");
        requireNotBlank(user.getLastName(), "lastName");
        requireNotBlank(email, "email");

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Un utilisateur avec cet email existe deja : " + email);
        }

        // Un id fourni par le client ne doit jamais permettre d'ecraser
        // une entite existante : on force une creation.
        user.setId(null);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec id=" + id));
    }

    /**
     * Mise a jour partielle : seuls les champs renseignes sont modifies.
     * L'email reste unique, sauf s'il s'agit de celui de l'utilisateur lui-meme.
     */
    @Transactional
    public User updateUser(Long id, User details) {
        User user = getUserById(id);

        if (details.getFirstName() != null) {
            requireNotBlank(details.getFirstName(), "firstName");
            user.setFirstName(details.getFirstName());
        }
        if (details.getLastName() != null) {
            requireNotBlank(details.getLastName(), "lastName");
            user.setLastName(details.getLastName());
        }
        if (details.getEmail() != null) {
            String email = normalizeEmail(details.getEmail());
            requireNotBlank(email, "email");
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new DuplicateResourceException(
                        "Un utilisateur avec cet email existe deja : " + email);
            }
            user.setEmail(email);
        }
        return userRepository.save(user);
    }

    /** La cascade User -> Board -> Task supprime aussi tableaux et taches. */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Le champ '" + field + "' ne doit pas etre vide");
        }
    }
}
