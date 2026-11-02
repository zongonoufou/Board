package com.example.Board.service;

import com.example.Board.exception.ResourceNotFoundException;
import com.example.Board.model.Board;
import com.example.Board.model.User;
import com.example.Board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Couche Service pour la gestion des tableaux (Board). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserService userService;

    /**
     * Cree un tableau pour un proprietaire existant.
     * L'id, la date de creation et le proprietaire sont imposes par le
     * serveur : aucune de ces valeurs n'est acceptee depuis le client.
     */
    @Transactional
    public Board createBoard(Long ownerId, Board board) {
        if (board.getTitle() == null || board.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre du tableau ne doit pas etre vide");
        }
        User owner = userService.getUserById(ownerId);

        board.setId(null);
        board.setCreatedAt(LocalDateTime.now());
        owner.addBoard(board); // synchronise aussi board.owner

        return boardRepository.save(board);
    }

    /** Tous les tableaux (utile quand aucun ownerId n'est fourni). */
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    /** Tableaux d'un utilisateur ; 404 si le proprietaire n'existe pas. */
    public List<Board> getBoardsByOwner(Long ownerId) {
        userService.getUserById(ownerId);
        return boardRepository.findByOwnerId(ownerId);
    }

    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tableau introuvable avec id=" + id));
    }

    /** Seul le titre est modifiable : owner et createdAt sont immuables. */
    @Transactional
    public Board updateBoard(Long id, Board details) {
        Board board = getBoardById(id);
        if (details.getTitle() == null || details.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre du tableau ne doit pas etre vide");
        }
        board.setTitle(details.getTitle());
        return boardRepository.save(board);
    }

    /** cascade = ALL + orphanRemoval : les taches sont supprimees aussi. */
    @Transactional
    public void deleteBoard(Long id) {
        Board board = getBoardById(id);
        boardRepository.delete(board);
    }
}
