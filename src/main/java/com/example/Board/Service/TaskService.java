package com.example.Board.service;
import com.example.Board.model.Board;
import com.example.Board.model.Task;
import com.example.Board.model.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Couche Service pour la gestion des taches (Task).
 *
 * Ce service illustre parfaitement le flux : le Controller appelle le Service,
 * le Service appelle le Repository, le Repository parle a la base de donnees.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardService boardService;

    /**
     * Creer une nouvelle tache dans un tableau.
     * On verifie que le tableau existe, puis on impose le statut initial A_FAIRE.
     *
     * @param boardId l'identifiant du tableau
     * @param task    la tache a creer
     */
    public Task createTask(Long boardId, Task task) {
        // On verifie que le tableau existe (leve une exception sinon)
        Board board = boardService.getBoardById(boardId);

        // On impose le statut initial : une nouvelle tache est toujours A_FAIRE
        task.setStatus(TaskStatus.A_FAIRE);
        task.setBoard(board);

        return taskRepository.save(task);
    }

    /**
     * Recuperer toutes les taches d'un tableau.
     */
    public List<Task> getTasksByBoard(Long boardId) {
        return taskRepository.findByBoardId(boardId);
    }

    /**
     * Recuperer une tache par son identifiant.
     */
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tache introuvable avec id=" + id));
    }

    /**
     * Mettre a jour une tache (titre, description, statut).
     * Hibernate detecte automatiquement les changements et les persiste
     * a la fin de la transaction ("dirty checking").
     *
     * @param id          identifiant de la tache a modifier
     * @param taskDetails les nouvelles valeurs
     */
    public Task updateTask(Long id, Task taskDetails) {
        Task task = getTaskById(id);
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        return taskRepository.save(task);
    }

    /**
     * Supprimer une tache.
     */
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
}
package com.example.Board.service;

import com.example.Board.model.Board;
import com.example.Board.model.Task;
import com.example.Board.model.TaskStatus;
import com.example.Board.repository.TaskRepository;
