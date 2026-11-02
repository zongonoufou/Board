package com.example.Board.mapper;

import com.example.Board.dto.BoardRequest;
import com.example.Board.dto.BoardResponse;
import com.example.Board.dto.BoardUpdateRequest;
import com.example.Board.model.Board;

/** Traduction explicite DTO &lt;-&gt; entite pour les tableaux. */
public final class BoardMapper {

    private BoardMapper() {
    }

    public static Board toEntity(BoardRequest request) {
        Board board = new Board();
        board.setTitle(request.getTitle());
        // id, createdAt, owner et tasks restent aux valeurs par defaut :
        // c'est BoardService qui les impose.
        return board;
    }

    public static Board toEntity(BoardUpdateRequest request) {
        Board board = new Board();
        board.setTitle(request.getTitle());
        return board;
    }

    public static BoardResponse toResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getCreatedAt(),
                board.getOwnerId());
    }
}
