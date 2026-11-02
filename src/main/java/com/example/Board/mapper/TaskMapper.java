package com.example.Board.mapper;

import com.example.Board.dto.TaskRequest;
import com.example.Board.dto.TaskResponse;
import com.example.Board.dto.TaskUpdateRequest;
import com.example.Board.model.Task;

/** Traduction explicite DTO &lt;-&gt; entite pour les taches. */
public final class TaskMapper {

    private TaskMapper() {
    }

    public static Task toEntity(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        // status, id et board ne sont PAS pris depuis le client.
        return task;
    }

    public static Task toEntity(TaskUpdateRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        return task;
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getBoardId());
    }
}
