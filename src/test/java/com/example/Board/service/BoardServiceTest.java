package com.example.Board.service;

import com.example.Board.exception.ResourceNotFoundException;
import com.example.Board.model.Board;
import com.example.Board.model.User;
import com.example.Board.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BoardService boardService;

    @Test
    void createBoardAssocieLeProprietaireEtImposeLaDate() {
        User owner = new User();
        owner.setId(7L);
        Board board = new Board();
        board.setTitle("Projet");
        board.setCreatedAt(LocalDateTime.of(2000, 1, 1, 0, 0));
        board.setId(123L);

        when(userService.getUserById(7L)).thenReturn(owner);
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Board created = boardService.createBoard(7L, board);

        assertThat(created.getOwner()).isSameAs(owner);
        assertThat(created.getId()).isNull();
        assertThat(created.getCreatedAt().getYear()).isGreaterThan(2000);
        assertThat(owner.getBoards()).contains(created);
    }

    @Test
    void createBoardRefuseUnTitreVide() {
        Board board = new Board();
        board.setTitle("  ");

        assertThatThrownBy(() -> boardService.createBoard(7L, board))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getBoardByIdLeveUneExceptionMetierQuandAbsent() {
        when(boardRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateBoardNeModifieQueLeTitre() {
        User owner = new User();
        owner.setId(7L);
        Board existing = new Board();
        existing.setId(1L);
        existing.setTitle("Ancien");
        existing.setOwner(owner);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        existing.setCreatedAt(createdAt);

        Board patch = new Board();
        patch.setTitle("Nouveau");

        when(boardRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Board updated = boardService.updateBoard(1L, patch);

        assertThat(updated.getTitle()).isEqualTo("Nouveau");
        assertThat(updated.getOwner()).isSameAs(owner);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
    }
}
