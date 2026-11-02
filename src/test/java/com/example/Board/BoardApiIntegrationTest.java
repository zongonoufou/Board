package com.example.Board;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de bout en bout : demarre l'application sur un port aleatoire avec
 * H2 en memoire et verifie les codes HTTP, la validation, les relations
 * et la serialisation JSON (pas de proxy Hibernate dans les reponses).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BoardApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void parcoursCompletEtGestionDesErreurs() {
        // Documentation : /docs est l'entree Swagger UI configuree dans application.properties
        ResponseEntity<String> docs = restTemplate.getForEntity("/docs", String.class);
        assertThat(docs.getStatusCode().is2xxSuccessful() || docs.getStatusCode().is3xxRedirection())
                .isTrue();
        assertThat(restTemplate.getForEntity("/v3/api-docs", String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", "Ada");
        user.put("lastName", "Lovelace");
        user.put("email", "ada.integration@example.com");

        ResponseEntity<JsonNode> createdUser =
                restTemplate.postForEntity("/api/users", user, JsonNode.class);
        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode userBody = createdUser.getBody();
        long userId = userBody.get("id").asLong();
        assertThat(userBody.get("firstName").asText()).isEqualTo("Ada");
        assertThat(userBody.has("boards")).isFalse();

        // Email en doublon -> 409
        assertThat(restTemplate.postForEntity("/api/users", user, JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // Corps invalide -> 400 avec les details de validation
        Map<String, Object> invalidUser = new HashMap<>();
        invalidUser.put("firstName", "");
        invalidUser.put("email", "pas-un-email");
        ResponseEntity<JsonNode> badUser =
                restTemplate.postForEntity("/api/users", invalidUser, JsonNode.class);
        assertThat(badUser.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(badUser.getBody().get("fieldErrors").size()).isGreaterThan(0);

        // URL inconnue -> 404 et non 500 (le handler global respecte ErrorResponse)
        ResponseEntity<JsonNode> unknown =
                restTemplate.getForEntity("/cette-page-nexiste-pas", JsonNode.class);
        assertThat(unknown.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(unknown.getBody().get("status").asInt()).isEqualTo(404);

        // Identifiant non numerique -> 400, methode HTTP interdite -> 405
        assertThat(restTemplate.getForEntity("/api/users/pas-un-id", JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.exchange("/api/users", HttpMethod.DELETE,
                new HttpEntity<>(new HashMap<>()), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);

        // Mise a jour partielle d'un utilisateur
        Map<String, Object> userPatch = new HashMap<>();
        userPatch.put("lastName", "Byron");
        ResponseEntity<JsonNode> updatedUser = restTemplate.exchange(
                "/api/users/" + userId, HttpMethod.PUT,
                new HttpEntity<>(userPatch), JsonNode.class);
        assertThat(updatedUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedUser.getBody().get("lastName").asText()).isEqualTo("Byron");
        assertThat(updatedUser.getBody().get("firstName").asText()).isEqualTo("Ada");

        // Creation d'un tableau (createdAt fourni par le client doit etre ignore)
        Map<String, Object> board = new HashMap<>();
        board.put("title", "Projet");
        board.put("createdAt", "2000-01-01T00:00:00");
        ResponseEntity<JsonNode> createdBoard = restTemplate.postForEntity(
                "/api/boards?ownerId=" + userId, board, JsonNode.class);
        assertThat(createdBoard.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode boardBody = createdBoard.getBody();
        long boardId = boardBody.get("id").asLong();
        assertThat(boardBody.get("ownerId").asLong()).isEqualTo(userId);
        assertThat(boardBody.has("tasks")).isFalse();
        assertThat(boardBody.get("createdAt").asText()).doesNotStartWith("2000-");

        // Proprietaire inconnu -> 404
        assertThat(restTemplate.postForEntity("/api/boards?ownerId=999999", board, JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Liste filtree et liste globale
        assertThat(restTemplate.getForEntity("/api/boards?ownerId=" + userId, JsonNode.class)
                .getBody().size()).isEqualTo(1);
        assertThat(restTemplate.getForEntity("/api/boards", JsonNode.class)
                .getBody().size()).isGreaterThanOrEqualTo(1);

        // Renommage d'un tableau
        Map<String, Object> boardPatch = new HashMap<>();
        boardPatch.put("title", "Projet renomme");
        ResponseEntity<JsonNode> renamed = restTemplate.exchange(
                "/api/boards/" + boardId, HttpMethod.PUT,
                new HttpEntity<>(boardPatch), JsonNode.class);
        assertThat(renamed.getBody().get("title").asText()).isEqualTo("Projet renomme");
        assertThat(renamed.getBody().get("ownerId").asLong()).isEqualTo(userId);

        // Creation d'une tache : le statut envoye est ignore (RG5)
        Map<String, Object> task = new HashMap<>();
        task.put("title", "Premiere tache");
        task.put("description", "Description");
        task.put("status", "TERMINE");
        ResponseEntity<JsonNode> createdTask = restTemplate.postForEntity(
                "/api/boards/" + boardId + "/tasks", task, JsonNode.class);
        assertThat(createdTask.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode taskBody = createdTask.getBody();
        long taskId = taskBody.get("id").asLong();
        assertThat(taskBody.get("status").asText()).isEqualTo("A_FAIRE");
        assertThat(taskBody.get("boardId").asLong()).isEqualTo(boardId);
        assertThat(taskBody.has("board")).isFalse();

        assertThat(restTemplate.getForEntity("/api/boards/" + boardId + "/tasks", JsonNode.class)
                .getBody().size()).isEqualTo(1);

        // Mise a jour partielle de la tache
        Map<String, Object> patch = new HashMap<>();
        patch.put("status", "EN_COURS");
        ResponseEntity<JsonNode> updated = restTemplate.exchange(
                "/api/boards/" + boardId + "/tasks/" + taskId,
                HttpMethod.PUT, new HttpEntity<>(patch), JsonNode.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("status").asText()).isEqualTo("EN_COURS");
        assertThat(updated.getBody().get("title").asText()).isEqualTo("Premiere tache");

        // Endpoint dedie au changement de statut
        Map<String, Object> statusPatch = new HashMap<>();
        statusPatch.put("status", "TERMINE");
        ResponseEntity<JsonNode> moved = restTemplate.exchange(
                "/api/boards/" + boardId + "/tasks/" + taskId + "/status",
                HttpMethod.PATCH, new HttpEntity<>(statusPatch), JsonNode.class);
        assertThat(moved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(moved.getBody().get("status").asText()).isEqualTo("TERMINE");

        // Une tache n'est pas accessible depuis un autre tableau -> 404
        assertThat(restTemplate.getForEntity(
                "/api/boards/999999/tasks/" + taskId, JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // Creation puis suppression d'une seconde tache
        Map<String, Object> secondTask = new HashMap<>();
        secondTask.put("title", "Seconde tache");
        ResponseEntity<JsonNode> createdSecond = restTemplate.postForEntity(
                "/api/boards/" + boardId + "/tasks", secondTask, JsonNode.class);
        long secondId = createdSecond.getBody().get("id").asLong();
        assertThat(restTemplate.exchange(
                "/api/boards/" + boardId + "/tasks/" + secondId,
                HttpMethod.DELETE, new HttpEntity<>(new HashMap<>()), Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(restTemplate.getForEntity("/api/boards/" + boardId + "/tasks", JsonNode.class)
                .getBody().size()).isEqualTo(1);

        // Suppression du tableau -> ses taches disparaissent
        restTemplate.delete("/api/boards/" + boardId);
        assertThat(restTemplate.getForEntity("/api/boards/" + boardId, JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.getForEntity("/api/boards/" + boardId + "/tasks", JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Suppression de l'utilisateur puis 404
        restTemplate.delete("/api/users/" + userId);
        assertThat(restTemplate.getForEntity("/api/users/" + userId, JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.getForEntity("/api/users/999999", JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
