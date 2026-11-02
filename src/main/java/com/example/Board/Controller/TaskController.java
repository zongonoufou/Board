package com.example.Board.controller;

import com.example.Board.model.Task;
import com.example.Board.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Couche Controller : expose l'API REST pour les taches (Task).
 *
 * Les taches sont rattachees a un tableau dans l'URL (/api/boards/{boardId}/tasks).
 * Cela reflete la relation Board (1) -- (0..*) Task :
 * on ne peut pas creer une tache sans la rattacher a un tableau.
 *
 * Endpoints disponibles :
 *   POST   /api/boards/{boardId}/tasks           -> creer une tache
 *   GET    /api/boards/{boardId}/tasks           -> lister les taches d'un tableau
 *   GET    /api/boards/{boardId}/tasks/{taskId}  -> recuperer une tache par id
 *   PUT    /api/boards/{boardId}/tasks/{taskId}  -> mettre a jour une tache
 *   DELETE /api/boards/{boardId}/tasks/{taskId}  -> supprimer une tache
 */
@RestController
@RequestMapping("/api/boards/{boardId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@PathVariable Long boardId,
                                           @RequestBody Task task) {
        Task created = taskService.createTask(boardId, task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(@PathVariable Long boardId) {
        return ResponseEntity.ok(taskService.getTasksByBoard(boardId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long boardId,
                                            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long boardId,
                                           @PathVariable Long taskId,
                                           @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(taskId, task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long boardId,
                                           @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
