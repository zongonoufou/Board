
import com.example.Board.service;
import com.example.Board.model.User;
import com.example.Board.userRepository;
import lombok.RequiredArgsConstructor;

import org.hibernate.annotations.GenericGenerators;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Couche Service : contient la LOGIQUE METIER.
 *
 * @Service marque cette classe comme un composant Spring injectable.
 * @RequiredArgsConstructor (Lombok) genere un constructeur prenant tous
 * les champs "final" en parametre -> c'est ainsi que Spring injecte
 * UserRepository (injection de dependances par constructeur).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Creer un nouvel utilisateur.
     * On verifie d'abord qu'aucun utilisateur n'existe deja avec le meme email.
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe deja : " + user.getEmail());
        }
        return userRepository.save(user);
    }

    /**
     * Recuperer tous les utilisateurs.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Recuperer un utilisateur par son identifiant.
     * orElseThrow() leve une exception si l'utilisateur n'est pas trouve.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec id=" + id));
    }
}
