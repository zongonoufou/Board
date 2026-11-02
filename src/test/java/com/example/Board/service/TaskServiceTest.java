package com.example.Board.service;

import com.example.Board.exception.ResourceNotFoundException;
import com.example.Board.model.Board;
import com.example.Board.model.Task;
import com.example.Board.model.TaskStatus;
import com.example.Board.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private TaskService taskService;

    private static Board board(long id) {
        Board board = new Board();
        board.setId(id);
        return board;
    }

    @Test
    void createTaskImposeLeStatutA_FAIRE() {
        Board board = board(1L);
        Task sent = new Task();
        sent.setTitle("Premiere tache");
        sent.setStatus(TaskStatus.TERMINE); // ignore volontairement

        when(boardService.getBoardById(1L)).thenReturn(board);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask(1L, sent);

        assertThat(created.getStatus()).isEqualTo(TaskStatus.A_FAIRE);
        assertThat(created.getBoard()).isSameAs(board);
        assertThat(board.getTasks()).contains(created);
    }

    @Test
    void createTaskRefuseUnTitreVide() {
        Task sent = new Task();
        sent.setTitle(" ");

        assertThatThrownBy(() -> taskService.createTask(1L, sent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTaskByIdVerifieLAppartenanceAuTableau() {
        when(boardService.getBoardById(1L)).thenReturn(board(1L));
        when(taskRepository.findByIdAndBoardId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTaskNeModifieQueLesChampsFournis() {
        Board board = board(1L);
        Task existing = new Task();
        existing.setId(5L);
        existing.setTitle("Ancien titre");
        existing.setDescription("Description");
        existing.setStatus(TaskStatus.A_FAIRE);
        existing.setBoard(board);

        Task patch = new Task();
        patch.setStatus(TaskStatus.EN_COURS);

        when(boardService.getBoardById(1L)).thenReturn(board);
        when(taskRepository.findByIdAndBoardId(5L, 1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskService.updateTask(1L, 5L, patch);

        assertThat(updated.getTitle()).isEqualTo("Ancien titre");
        assertThat(updated.getDescription()).isEqualTo("Description");
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.EN_COURS);
        assertThat(updated.getBoard()).isSameAs(board);
    }

    @Test
    void changeStatusDeplaceLaTache() {
        Board board = board(1L);
        Task existing = new Task();
        existing.setId(5L);
        existing.setTitle("Tache");
        existing.setStatus(TaskStatus.A_FAIRE);
        existing.setBoard(board);

        when(boardService.getBoardById(1L)).thenReturn(board);
        when(taskRepository.findByIdAndBoardId(5L, 1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskService.changeStatus(1L, 5L, TaskStatus.TERMINE);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.TERMINE);
    }
}
