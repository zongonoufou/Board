package com.example.Board.controller;

import com.example.Board.dto.StatusUpdateRequest;
import com.example.Board.dto.TaskRequest;
import com.example.Board.dto.TaskResponse;
import com.example.Board.dto.TaskUpdateRequest;
import com.example.Board.mapper.TaskMapper;
import com.example.Board.model.Task;
import com.example.Board.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Couche Controller : expose l'API REST pour les taches (Task).
 *
 * Les taches sont rattachees a un tableau dans l'URL :
 * /api/boards/{boardId}/tasks. Le boardId est transmis au Service pour
 * toutes les operations : on ne peut pas manipuler une tache depuis
 * l'URL d'un tableau auquel elle n'appartient pas.
 *
 * POST   /api/boards/{boardId}/tasks                    -> creer
 * GET    /api/boards/{boardId}/tasks                    -> lister
 * GET    /api/boards/{boardId}/tasks/{taskId}           -> lire
 * PUT    /api/boards/{boardId}/tasks/{taskId}           -> modifier (partiel)
 * PATCH  /api/boards/{boardId}/tasks/{taskId}/status    -> changer le statut
 * DELETE /api/boards/{boardId}/tasks/{taskId}           -> supprimer
 */
@RestController
@RequestMapping("/api/boards/{boardId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TaskResponse> createTask(@PathVariable("boardId") Long boardId,
                                                   @Valid @RequestBody TaskRequest request) {
        Task created = taskService.createTask(boardId, TaskMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable("boardId") Long boardId) {
        return ResponseEntity.ok(taskService.getTasksByBoard(boardId).stream()
                .map(TaskMapper::toResponse).toList());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable("boardId") Long boardId,
                                                    @PathVariable("taskId") Long taskId) {
        return ResponseEntity.ok(TaskMapper.toResponse(taskService.getTaskById(boardId, taskId)));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable("boardId") Long boardId,
                                                   @PathVariable("taskId") Long taskId,
                                                   @Valid @RequestBody TaskUpdateRequest request) {
        Task updated = taskService.updateTask(boardId, taskId, TaskMapper.toEntity(request));
        return ResponseEntity.ok(TaskMapper.toResponse(updated));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> changeTaskStatus(@PathVariable("boardId") Long boardId,
                                                         @PathVariable("taskId") Long taskId,
                                                         @Valid @RequestBody StatusUpdateRequest request) {
        Task updated = taskService.changeStatus(boardId, taskId, request.getStatus());
        return ResponseEntity.ok(TaskMapper.toResponse(updated));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable("boardId") Long boardId,
                                           @PathVariable("taskId") Long taskId) {
        taskService.deleteTask(boardId, taskId);
        return ResponseEntity.noContent().build();
    }
}
