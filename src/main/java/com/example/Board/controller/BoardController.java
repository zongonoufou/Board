package com.example.Board.controller;

import com.example.Board.dto.BoardRequest;
import com.example.Board.dto.BoardResponse;
import com.example.Board.dto.BoardUpdateRequest;
import com.example.Board.mapper.BoardMapper;
import com.example.Board.model.Board;
import com.example.Board.service.BoardService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Couche Controller : expose l'API REST pour les tableaux (Board).
 * Meme principe que UserController : DTO en entree, DTO en sortie.
 *
 * POST   /api/boards?ownerId={id}  -> creer un tableau
 * GET    /api/boards               -> lister tous les tableaux
 * GET    /api/boards?ownerId={id}  -> lister les tableaux d'un utilisateur
 * GET    /api/boards/{id}          -> recuperer un tableau par id
 * PUT    /api/boards/{id}          -> renommer un tableau
 * DELETE /api/boards/{id}          -> supprimer un tableau (et ses taches)
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BoardResponse> createBoard(@RequestParam("ownerId") Long ownerId,
                                                     @Valid @RequestBody BoardRequest request) {
        Board created = boardService.createBoard(ownerId, BoardMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(BoardMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoards(
            @RequestParam(name = "ownerId", required = false) Long ownerId) {
        List<Board> boards = (ownerId == null)
                ? boardService.getAllBoards()
                : boardService.getBoardsByOwner(ownerId);
        return ResponseEntity.ok(boards.stream().map(BoardMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(BoardMapper.toResponse(boardService.getBoardById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> updateBoard(@PathVariable("id") Long id,
                                                     @Valid @RequestBody BoardUpdateRequest request) {
        Board updated = boardService.updateBoard(id, BoardMapper.toEntity(request));
        return ResponseEntity.ok(BoardMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
