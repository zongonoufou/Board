package com.example.Board.service;

import com.example.Board.model.Board;
import com.example.Board.model.User;
import com.example.Board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Couche Service pour la gestion des tableaux (Board).
 *
 * Ce service contient la logique metier : verifier que le proprietaire
 * existe avant de creer un tableau, supprimer un tableau, etc.
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserService userService;

    /**
     * Creer un nouveau tableau pour un utilisateur.
     * On verifie que le proprietaire (owner) existe avant de sauvegarder.
     *
     * @param ownerId l'identifiant du proprietaire
     * @param board   le tableau a creer (sans owner pour l'instant)
     */
    public Board createBoard(Long ownerId, Board board) {
        // On verifie que l'utilisateur existe (leve une exception sinon)
        User owner = userService.getUserById(ownerId);
        board.setOwner(owner);
        return boardRepository.save(board);
    }

    /**
     * Recuperer tous les tableaux d'un utilisateur.
     */
    public List<Board> getBoardsByOwner(Long ownerId) {
        return boardRepository.findByOwnerId(ownerId);
    }

    /**
     * Recuperer un tableau par son identifiant.
     */
    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tableau introuvable avec id=" + id));
    }

    /**
     * Supprimer un tableau.
     * Grace a cascade = ALL + orphanRemoval sur Board.tasks,
     * toutes les taches associees sont aussi supprimees automatiquement.
     */
    public void deleteBoard(Long id) {
        Board board = getBoardById(id);
        boardRepository.delete(board);
    }
}
