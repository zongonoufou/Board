package com.example.Board.service;

import com.example.Board.exception.ResourceNotFoundException;
import com.example.Board.model.Board;
import com.example.Board.model.Task;
import com.example.Board.model.TaskStatus;
import com.example.Board.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Couche Service pour la gestion des taches (Task).
 *
 * Toutes les operations sont rattachees a un boardId : une tache ne peut
 * pas etre lue, modifiee ou supprimee via l'URL d'un autre tableau.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardService boardService;

    /**
     * Cree une tache dans un tableau existant.
     * RG5 : le statut initial est toujours A_FAIRE, meme si le client
     * envoie une autre valeur.
     */
    @Transactional
    public Task createTask(Long boardId, Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre de la tache ne doit pas etre vide");
        }
        Board board = boardService.getBoardById(boardId);

        task.setId(null);
        task.setStatus(TaskStatus.A_FAIRE);
        board.addTask(task); // synchronise aussi task.board

        return taskRepository.save(task);
    }

    /** Liste les taches d'un tableau ; 404 si le tableau n'existe pas. */
    public List<Task> getTasksByBoard(Long boardId) {
        boardService.getBoardById(boardId);
        return taskRepository.findByBoardId(boardId);
    }

    /** Recupere une tache en verifiant son appartenance au tableau. */
    public Task getTaskById(Long boardId, Long taskId) {
        boardService.getBoardById(boardId);
        return taskRepository.findByIdAndBoardId(taskId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tache introuvable avec id=" + taskId
                                + " dans le tableau id=" + boardId));
    }

    /**
     * Mise a jour partielle : un champ null signifie "ne pas modifier".
     * Le tableau de rattachement n'est jamais modifiable par ce biais.
     */
    @Transactional
    public Task updateTask(Long boardId, Long taskId, Task details) {
        Task task = getTaskById(boardId, taskId);

        if (details.getTitle() != null) {
            if (details.getTitle().isBlank()) {
                throw new IllegalArgumentException("Le titre de la tache ne doit pas etre vide");
            }
            task.setTitle(details.getTitle());
        }
        if (details.getDescription() != null) {
            task.setDescription(details.getDescription());
        }
        if (details.getStatus() != null) {
            task.setStatus(details.getStatus());
        }
        return taskRepository.save(task);
    }

    /** Deplacement d'une tache entre les colonnes du tableau. */
    @Transactional
    public Task changeStatus(Long boardId, Long taskId, TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Le statut est obligatoire");
        }
        Task task = getTaskById(boardId, taskId);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long boardId, Long taskId) {
        Task task = getTaskById(boardId, taskId);
        taskRepository.delete(task);
    }
}
