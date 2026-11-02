package com.example.Board.controller;

import com.example.Board.model.Board;
import com.example.Board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Couche Controller : expose l'API REST pour les tableaux (Board).
 *
 * Endpoints disponibles :
 *   POST   /api/boards?ownerId={id}  -> creer un tableau pour un utilisateur
 *   GET    /api/boards?ownerId={id}  -> lister les tableaux d'un utilisateur
 *   GET    /api/boards/{id}          -> recuperer un tableau par id
 *   DELETE /api/boards/{id}          -> supprimer un tableau
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * Creer un tableau.
     * On passe l'ownerId en parametre de requete (query param).
     * Exemple : POST /api/boards?ownerId=1
     */
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestParam Long ownerId,
                                             @RequestBody Board board) {
        Board created = boardService.createBoard(ownerId, board);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lister les tableaux d'un utilisateur.
     * Exemple : GET /api/boards?ownerId=1
     */
    @GetMapping
    public ResponseEntity<List<Board>> getBoards(@RequestParam Long ownerId) {
        return ResponseEntity.ok(boardService.getBoardsByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
