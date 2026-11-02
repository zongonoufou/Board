# Board — API REST de tableaux / tâches

Application Spring Boot 3.5.6 (Java 25) organisée en couches
`Controller → Service → Repository → Model`, avec JPA/H2 et validation.

## 1. Lancer le projet

Prérequis : **JDK 25** (le `pom.xml` fixe `<java.version>25</java.version>`).

```bash
./mvnw clean verify          # compilation + tous les tests
./mvnw spring-boot:run       # démarrage sur http://localhost:8080
```

- Swagger UI : `http://localhost:8080/docs` (JSON OpenAPI : `/v3/api-docs`)
- Console H2 : `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:boarddb`, user `sa`)

La base H2 est en mémoire et le schéma est créé automatiquement
(`spring.jpa.hibernate.ddl-auto=update`). PostgreSQL est disponible comme
dépendance `runtime` pour un déploiement réel.

## 2. Erreurs corrigées

| # | Problème | Correction |
|---|----------|------------|
| 1 | `UserService` n'avait **pas de déclaration `package`** et importait `com.example.Board.service`, `com.example.Board.userRepository` et `org.hibernate.annotations.GenericGenerators` (inexistant en Hibernate 6). La classe était donc dans le package par défaut et introuvable. | Ajout de `package com.example.Board.service;` et des imports corrects (`com.example.Board.repository.UserRepository`), suppression de l'import Hibernate obsolète. |
| 2 | `TaskService` contenait un **second bloc `package`/`import` après l'accolade fermante** et n'importait pas `TaskRepository`. | Fichier réécrit : import manquant ajouté, code parasite supprimé. |
| 3 | `TaskStatus` déclarait `TODO, IN_PROGRESS, DONE` alors que `Task` et `TaskService` utilisaient `A_FAIRE, EN_COURS, TERMINE` → `cannot find symbol`. | Enum aligné sur `A_FAIRE / EN_COURS / TERMINE` (valeurs du contrat JSON et de la documentation). |
| 4 | Dossiers `Controller/`, `Model/`, `Repository/`, `Service/` en majuscules alors que les packages déclarés sont en minuscules (non portable sur un FS sensible à la casse). | Dossiers renommés en minuscules pour correspondre aux packages. |
| 5 | `User.FirstName` / `User.LastName` ne respectaient pas les conventions Java. | Renommés `firstName` / `lastName` avec noms de colonnes explicites. |
| 6 | `RuntimeException` brutes → **HTTP 500** pour un simple `404`/conflit d'email. | Exceptions métier `ResourceNotFoundException`, `DuplicateResourceException` + `GlobalExceptionHandler` (`404`, `409`, `400`, `500`) et corps d'erreur `ApiError` homogène. |
| 7 | Sérialisation JSON des associations `LAZY` (`LazyInitializationException` / `ByteBuddyInterceptor`) et `open-in-view` activé par défaut. | `@JsonIgnore` sur les associations + propriétés en lecture seule `ownerId` / `boardId`, `@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})`, `spring.jpa.open-in-view=false`. |
| 8 | `TaskController` ignorait le `boardId` de l'URL : une tâche était lue/modifiée/supprimée depuis l'URL de **n'importe quel** tableau. | Toutes les opérations passent par le Service avec `boardId` ; requête `findByIdAndBoardId`. |
| 9 | Statut d'une tâche écrasé par `null` lors d'un `PUT` partiel → violation de contrainte en base. | Mise à jour partielle : seuls les champs non nuls sont appliqués. |
| 10 | Un client pouvait fournir `id`, `createdAt`, `owner` ou `board` dans le JSON et écraser une entité. | Ces champs sont imposés par le serveur ; `createdAt` est généré côté service et par `@PrePersist`. |
| 11 | Aucune validation des entrées. | `spring-boot-starter-validation` + `@Valid`, `@NotBlank`, `@Email`, `@Size`, `@NotNull`. |
| 12 | Absence de transactions et de annotation processor Lombok explicite (depuis le JDK 23, `javac` ne découvre plus les processors implicitement → compilation cassée). | `@Transactional` sur les services et `<annotationProcessorPaths>` Lombok dans `maven-compiler-plugin`. |
| 13 | `GET /api/boards/{id}/tasks` renvoyait `200 []` pour un tableau inexistant. | Vérification de l'existence du tableau → `404`. |
| 14 | Les getters JSON `getOwnerId()` / `getBoardId()` faisaient échouer les requêtes dérivées Spring Data au démarrage (`Could not resolve attribute 'ownerId'`). | Requêtes JPQL explicites (`@Query`) dans `BoardRepository` et `TaskRepository`. |
| 15 | `@PathVariable` / `@RequestParam` sans nom dépendaient du flag de compilation `-parameters`. | Noms explicites dans les annotations + `<parameters>true</parameters>` dans `maven-compiler-plugin`. |
| 16 | Le `@ExceptionHandler(Exception.class)` « fourre-tout » transformait les exceptions Spring MVC en **500** : une URL inconnue (`/docs`, `/toto`) renvoyait 500 au lieu de 404. | Le gestionnaire respecte désormais le statut porté par `ErrorResponse` (404, 405, 400, 415…), ne journalise en 500 que les vraies erreurs inattendues, et `/docs` est configuré comme entrée Swagger UI. |

## 3. Méthodes et endpoints ajoutés

**Services** : `UserService.updateUser`, `UserService.deleteUser`,
`BoardService.getAllBoards`, `BoardService.updateBoard`,
`TaskService.getTaskById(boardId, taskId)`, `TaskService.updateTask(boardId, …)`,
`TaskService.changeStatus`, `TaskService.deleteTask(boardId, …)`.

**Entités** : `User.addBoard` / `removeBoard`, `Board.removeTask`
(`Board.addTask` existait), `Board.getOwnerId`, `Task.getBoardId`, `@PrePersist`.

**Repositories** : `UserRepository.existsByEmailAndIdNot`,
`TaskRepository.findByIdAndBoardId`.

| Verbe | URL | Description |
|-------|-----|-------------|
| POST | `/api/users` | Créer un utilisateur (email unique) |
| GET | `/api/users`, `/api/users/{id}` | Lister / lire |
| PUT | `/api/users/{id}` | Modifier (partiel) |
| DELETE | `/api/users/{id}` | Supprimer (cascade tableaux + tâches) |
| POST | `/api/boards?ownerId={id}` | Créer un tableau |
| GET | `/api/boards` | Lister tous les tableaux |
| GET | `/api/boards?ownerId={id}` | Lister les tableaux d'un utilisateur |
| GET / PUT / DELETE | `/api/boards/{id}` | Lire / renommer / supprimer |
| POST | `/api/boards/{boardId}/tasks` | Créer une tâche (statut forcé `A_FAIRE`) |
| GET | `/api/boards/{boardId}/tasks` | Lister les tâches du tableau |
| GET / PUT / DELETE | `/api/boards/{boardId}/tasks/{taskId}` | Lire / modifier / supprimer |
| PATCH | `/api/boards/{boardId}/tasks/{taskId}/status` | `{"status":"EN_COURS"}` |

Statuts valides : `A_FAIRE`, `EN_COURS`, `TERMINE`.

Exemple de création :

```bash
curl -X POST 'http://localhost:8080/api/users' \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}'

curl -X POST 'http://localhost:8080/api/boards?ownerId=1' \
  -H 'Content-Type: application/json' -d '{"title":"Projet"}'

curl -X POST 'http://localhost:8080/api/boards/1/tasks' \
  -H 'Content-Type: application/json' -d '{"title":"Première tâche","description":"…"}'
```

## 4. Réponses d'erreur

`GlobalExceptionHandler` renvoie un corps homogène
`{timestamp, status, error, message, path, fieldErrors}` :

| Code | Cas |
|------|-----|
| 400 | validation `@Valid`, JSON illisible, paramètre mal typé |
| 404 | utilisateur/tableau/tâche absent **ou URL inconnue** |
| 405 | méthode HTTP non autorisée |
| 409 | email déjà utilisé, contrainte d'intégrité |
| 500 | erreur inattendue (stack trace journalisée côté serveur, jamais exposée) |

Le gestionnaire « fourre-tout » respecte le statut porté par les exceptions
Spring MVC (`ErrorResponse`) : une URL inexistante renvoie bien **404**, pas 500.

## 5. Tests

- `UserServiceTest`, `BoardServiceTest`, `TaskServiceTest` : tests unitaires
  Mockito (unicité email, statut imposé, mise à jour partielle, rattachement au tableau).
- `BoardApiIntegrationTest` : test de bout en bout sur port aléatoire
  (codes HTTP, validation, CRUD complet, changement de statut, cascade de
  suppression, sérialisation JSON).
- `BoardApplicationTests` : chargement du contexte Spring.

Soit **16 tests**, tous verts (`./mvnw clean verify`).

## 6. Piste d'amélioration

Pour une API publique, remplacer l'exposition directe des entités JPA par des
DTO de requête/réponse afin de découpler complètement le contrat HTTP du
modèle de persistance.
