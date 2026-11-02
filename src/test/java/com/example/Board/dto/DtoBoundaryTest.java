package com.example.Board.dto;

import com.example.Board.mapper.BoardMapper;
import com.example.Board.mapper.TaskMapper;
import com.example.Board.mapper.UserMapper;
import com.example.Board.model.Board;
import com.example.Board.model.Task;
import com.example.Board.model.TaskStatus;
import com.example.Board.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la FRONTIERE DTO (aucun contexte Spring, tres rapides).
 *
 * Ils verifient la propriete la plus importante d'un DTO : ce que le client
 * envoie ne peut pas piloter les champs que le serveur doit controler, et
 * ce que le serveur renvoie ne contient aucune entite JPA ni relation.
 */
class DtoBoundaryTest {

    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void userRequestIgnoreIdEtRelations() throws Exception {
        UserRequest request = json.readValue(
                "{\"id\":999,\"boards\":[{\"id\":1}],\"firstName\":\"Ada\","
                        + "\"lastName\":\"Lovelace\",\"email\":\"ada@example.com\"}",
                UserRequest.class);

        User entity = UserMapper.toEntity(request);
        assertThat(entity.getId()).isNull();              // pas de mass assignment
        assertThat(entity.getBoards()).isEmpty();
        assertThat(entity.getFirstName()).isEqualTo("Ada");
    }

    @Test
    void boardRequestIgnoreCreatedAtOwnerEtTasks() throws Exception {
        BoardRequest request = json.readValue(
                "{\"id\":999,\"title\":\"Projet\",\"createdAt\":\"2000-01-01T00:00:00\","
                        + "\"ownerId\":42,\"tasks\":[{\"id\":1}]}",
                BoardRequest.class);

        Board entity = BoardMapper.toEntity(request);
        assertThat(entity.getTitle()).isEqualTo("Projet");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getOwner()).isNull();
        assertThat(entity.getTasks()).isEmpty();
    }

    @Test
    void taskRequestIgnoreStatutIdEtBoardId() throws Exception {
        TaskRequest request = json.readValue(
                "{\"id\":999,\"title\":\"Tache\",\"description\":\"...\","
                        + "\"status\":\"TERMINE\",\"boardId\":42}",
                TaskRequest.class);

        Task entity = TaskMapper.toEntity(request);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getBoard()).isNull();
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.A_FAIRE); // valeur par defaut de l'entite
        assertThat(entity.getTitle()).isEqualTo("Tache");
    }

    @Test
    void userResponseNeContientPasLesRelations() throws Exception {
        User user = new User();
        user.setId(7L);
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setEmail("ada@example.com");
        user.getBoards().add(new Board());

        JsonNode body = json.valueToTree(UserMapper.toResponse(user));
        assertThat(body.get("id").asLong()).isEqualTo(7L);
        assertThat(body.get("email").asText()).isEqualTo("ada@example.com");
        assertThat(body.has("boards")).isFalse();
        Set<String> fields = new HashSet<>();
        body.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder("id", "firstName", "lastName", "email");
    }

    @Test
    void boardResponseExposeOwnerIdSansEntiteUser() throws Exception {
        User owner = new User();
        owner.setId(7L);
        Board board = new Board();
        board.setId(3L);
        board.setTitle("Projet");
        board.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        board.setOwner(owner);
        board.getTasks().add(new Task());

        JsonNode body = json.valueToTree(BoardMapper.toResponse(board));
        assertThat(body.get("ownerId").asLong()).isEqualTo(7L);
        assertThat(body.get("createdAt").asText()).startsWith("2026-01-01");
        assertThat(body.has("owner")).isFalse();
        assertThat(body.has("tasks")).isFalse();
    }

    @Test
    void taskResponseExposeBoardIdEtStatutSansEntiteBoard() throws Exception {
        Board board = new Board();
        board.setId(3L);
        Task task = new Task();
        task.setId(9L);
        task.setTitle("Tache");
        task.setStatus(TaskStatus.EN_COURS);
        task.setBoard(board);

        JsonNode body = json.valueToTree(TaskMapper.toResponse(task));
        assertThat(body.get("boardId").asLong()).isEqualTo(3L);
        assertThat(body.get("status").asText()).isEqualTo("EN_COURS");
        assertThat(body.has("board")).isFalse();
    }

    @Test
    void misesAJourPartiellesLaissentLesChampsAbsentsANull() throws Exception {
        UserUpdateRequest userPatch =
                json.readValue("{\"lastName\":\"Byron\"}", UserUpdateRequest.class);
        assertThat(userPatch.getFirstName()).isNull();
        assertThat(userPatch.getEmail()).isNull();
        assertThat(UserMapper.toEntity(userPatch).getFirstName()).isNull();

        BoardUpdateRequest boardPatch =
                json.readValue("{\"title\":\"Renomme\"}", BoardUpdateRequest.class);
        assertThat(boardPatch.getTitle()).isEqualTo("Renomme");

        TaskUpdateRequest taskPatch =
                json.readValue("{\"status\":\"TERMINE\"}", TaskUpdateRequest.class);
        assertThat(taskPatch.getTitle()).isNull();
        assertThat(taskPatch.getStatus()).isEqualTo(TaskStatus.TERMINE);
        assertThat(taskPatch.getDescription()).isNull();
    }
}
