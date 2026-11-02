# 🧪 Tester le projet Board — pas à pas

> Objectif : savoir **exactement** quoi taper, quoi regarder, et **ce que ça prouve**.
> Commandes données pour **PowerShell** (votre environnement) ; les équivalents
> Git Bash sont indiqués quand ils diffèrent.
> Pour le *pourquoi*, voir [`APPRENDRE.md`](APPRENDRE.md) — sections **B.6**
> (priorité des propriétés), **B.7** (JDK / bytecode), **B.8** (PostgreSQL).

---

## Les 3 niveaux

| Niveau | Durée | Ce que ça prouve | Prérequis |
|--------|-------|------------------|-----------|
| **1. Tests automatiques** | ~1 min | Compilation, 16 tests, contexte Spring, API HTTP | JDK 17+ |
| **2. Application sur H2** | ~5 min | Swagger, endpoints, codes d'erreur, données en mémoire | — |
| **3. Application sur PostgreSQL** | ~10 min | Driver PG, schéma réel, **persistance après redémarrage** | PostgreSQL démarré |

> ⚠️ Rappel important : le **niveau 1 tourne toujours sur H2**, même si vous avez
> configuré PostgreSQL sur votre machine. C'est voulu (tests hermétiques, B.6).
> Pour tester PostgreSQL, c'est le **niveau 3**.

---

## 0. Préparation (une seule fois)

```powershell
cd C:\Users\Dell\Documents\Backend\Board-corrige
git pull --ff-only origin arena/59f43911-board
java -version            # doit afficher 17 ou plus
.\mvnw.cmd -version      # affiche Maven ET le JDK réellement utilisés
```

`mvnw` (avec le `w`) est le Maven fourni par le projet. `mvn` sans `w` n'existe
pas ici. En Git Bash : `./mvnw -version`.

---

## Niveau 1 — Les tests automatiques (le plus rapide)

```powershell
.\mvnw.cmd clean verify
```

**Ce que vous devez voir** (et rien d'autre) :

```text
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Les rapports détaillés sont dans `target\surefire-reports\` (un `.txt` par classe).
Les 14 tests Mockito vérifient les règles métier ; les 2 tests `@SpringBootTest`
démarrent un vrai contexte Spring + Tomcat + H2 et appellent l'API.

### Variante pédagogique : prouver que les tests sont hermétiques

Même avec une configuration PostgreSQL hostile, le résultat ne bouge pas :

```powershell
$env:SPRING_PROFILES_ACTIVE = "postgres"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://127.0.0.1:1/board"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "faux"
.\mvnw.cmd clean verify          # toujours 16 / 16

Remove-Item Env:SPRING_PROFILES_ACTIVE
Remove-Item Env:SPRING_DATASOURCE_URL
Remove-Item Env:SPRING_DATASOURCE_USERNAME
Remove-Item Env:SPRING_DATASOURCE_PASSWORD
```

C'est la démonstration du rang 1 de B.6 : `@SpringBootTest(properties = …)`
écrase **même** les variables d'environnement.

---

## Niveau 2 — Démarrer l'application sur H2 et tester l'API

### 2.1 Démarrer (terminal n°1, laissé ouvert)

```powershell
.\mvnw.cmd spring-boot:run
```

Attendez `Started BoardApplication …` et `Tomcat started on port 8080`.
`spring-boot:run` **bloque** le terminal : ouvrez-en un **deuxième** pour la suite.

### 2.2 La documentation (2 vérifications)

```powershell
curl.exe -i http://localhost:8080/docs

$api = Invoke-RestMethod http://localhost:8080/v3/api-docs
$api.openapi                                                  # 3.1.0
$api.paths.PSObject.Properties.Name                            # les 7 chemins
$api.paths.'/api/users'.post.responses.PSObject.Properties.Name  # doit contenir 201
$api.components.schemas.Task.properties.status.PSObject.Properties['enum'].Value  # A_FAIRE, EN_COURS, TERMINE
```

- `/docs` → **302** vers `/swagger-ui/index.html` (ouvrez aussi l'URL dans le navigateur).
- `/v3/api-docs` → JSON **OpenAPI 3.1.0**, 7 chemins. Vérifiez que les `POST`
  annoncent bien `201` et que `Task.status` contient l'enum
  `A_FAIRE / EN_COURS / TERMINE`.

> Sous PowerShell, `curl` est un **alias** de `Invoke-WebRequest` : écrivez
> toujours `curl.exe`. En Git Bash, `curl` suffit.

### 2.3 Scénario complet (terminal n°2)

On capture les identifiants au lieu de supposer `1` — ça marche même sur une base
déjà utilisée :

```powershell
$u = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/users' `
  -ContentType 'application/json' `
  -Body '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}'
"user  -> id=$($u.id)  email=$($u.email)"

$b = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/boards?ownerId=$($u.id)" `
  -ContentType 'application/json' -Body '{"title":"Projet"}'
"board -> id=$($b.id)  ownerId=$($b.ownerId)  createdAt=$($b.createdAt)"

$t = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/boards/$($b.id)/tasks" `
  -ContentType 'application/json' -Body '{"title":"Premiere tache","description":"Verifier le 201"}'
"task  -> id=$($t.id)  status=$($t.status)  boardId=$($t.boardId)"

Invoke-RestMethod -Method Patch `
  -Uri "http://localhost:8080/api/boards/$($b.id)/tasks/$($t.id)/status" `
  -ContentType 'application/json' -Body '{"status":"EN_COURS"}'

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/boards/$($b.id)/tasks"
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/boards?ownerId=$($u.id)"
```

**Ce que vous devez observer** :

| Étape | Attendu | Leçon |
|-------|---------|-------|
| POST user | `id` présent, `firstName=Ada` | 201, email normalisé |
| POST board | `ownerId` en lecture seule, `createdAt` ISO | le serveur impose le propriétaire et la date |
| POST task | `status = A_FAIRE` **même si vous envoyez `TERMINE`** | le statut initial est imposé par le service |
| PATCH status | `status = EN_COURS` | mise à jour partielle |
| GET tasks | 1 tâche, `boardId` en lecture seule | la tâche est rattachée au bon tableau |

Console H2 : `http://localhost:8080/h2-console` → JDBC `jdbc:h2:mem:boarddb`,
user `sa`, mot de passe vide.

### 2.4 Les codes d'erreur (à vérifier aussi)

```powershell
curl.exe -s -o NUL -w "URL inconnue       -> %{http_code}`n" http://localhost:8080/inconnu
curl.exe -s -o NUL -w "id non numerique   -> %{http_code}`n" http://localhost:8080/api/users/abc
curl.exe -s -o NUL -w "utilisateur absent -> %{http_code}`n" http://localhost:8080/api/users/999999
curl.exe -s -o NUL -w "methode interdite  -> %{http_code}`n" -X DELETE http://localhost:8080/api/users
curl.exe -s -o NUL -w "email en doublon   -> %{http_code}`n" -X POST 'http://localhost:8080/api/users' `
  -H 'Content-Type: application/json' `
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}'
curl.exe -s -o NUL -w "email invalide     -> %{http_code}`n" -X POST 'http://localhost:8080/api/users' `
  -H 'Content-Type: application/json' `
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"pas-un-email"}'
```

Attendu : **404, 400, 404, 405, 409, 400**. Chaque corps d'erreur est un
`{timestamp, status, error, message, path, fieldErrors}` homogène.

### 2.5 La limite de H2 (à constater)

Arrêtez l'application (`Ctrl+C`) et relancez-la : `GET /api/users` renvoie `[]`.
H2 est **en mémoire** : tout disparaît. C'est pratique pour les tests, mais
c'est précisément ce que le niveau 3 va changer.

---

## Niveau 3 — Tester avec PostgreSQL

### 3.1 Créer une base de développement dédiée

```powershell
psql -U postgres -c "CREATE DATABASE board;"
# ou, si psql n'est pas dans le PATH :
# & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE DATABASE board;"
```

⚠️ `ddl-auto=update` **écrit le schéma** au démarrage : n'utilisez jamais une
base de production.

### 3.2 Activer le profil et démarrer

```powershell
$env:SPRING_PROFILES_ACTIVE = "postgres"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/board"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "votre_mot_de_passe"
.\mvnw.cmd spring-boot:run
```

Le fichier `application-postgres.properties` fournit déjà
`org.postgresql.Driver` : **ne remplacez pas seulement l'URL**, sinon HikariCP
garde le driver H2 et vous obtiendrez « claims to not accept jdbcUrl » (B.8).

### 3.3 Trois preuves que c'est vraiment PostgreSQL

```powershell
# 1) dans le journal du terminal n°1 (show-sql=true) : le DDL PostgreSQL
#    create table users (...), create table boards (...), create table tasks (...)
#    alter table ... add constraint ...
#    (en cas d'erreur de connexion, la trace nomme postgresql-42.7.7.jar)

# 2) le schéma existe
psql -U postgres -d board -c "\dt"
psql -U postgres -d board -c "\d tasks"

# 3) l'API écrit réellement (rejouer le scénario 2.3), puis :
psql -U postgres -d board -c "SELECT id, first_name, email FROM users;"
psql -U postgres -d board -c "SELECT id, title, status FROM tasks;"
```

### 3.4 Le test décisif : la persistance

1. Créez un utilisateur via le scénario 2.3.
2. `Ctrl+C` puis relancez `.\mvnw.cmd spring-boot:run` (les variables sont
   toujours actives dans la même session PowerShell).
3. `curl.exe http://localhost:8080/api/users` → l'utilisateur est **toujours là**.

C'est la différence observable avec le 2.5 : H2 en mémoire perd tout, PostgreSQL
conserve. **Voilà ce que « tester avec PostgreSQL » apporte réellement.**

### 3.5 Vérifier que les tests n'ont pas bougé

Sans rien désactiver, dans un autre terminal :

```powershell
.\mvnw.cmd clean verify     # 16 / 16, et le journal des tests dit jdbc:h2:mem:boarddb
```

### 3.6 Revenir à H2

```powershell
Remove-Item Env:SPRING_PROFILES_ACTIVE
Remove-Item Env:SPRING_DATASOURCE_URL
Remove-Item Env:SPRING_DATASOURCE_USERNAME
Remove-Item Env:SPRING_DATASOURCE_PASSWORD
```

Git Bash équivalent : `unset SPRING_PROFILES_ACTIVE SPRING_DATASOURCE_URL …`.

---

## ✅ Checklist finale

- [ ] `java -version` ≥ 17, `.\mvnw.cmd -version` affiche un JDK
- [ ] `.\mvnw.cmd clean verify` → `BUILD SUCCESS`, 16 tests
- [ ] `/docs` → 302, `/v3/api-docs` → JSON OpenAPI 3.1.0 avec `201` sur les POST
- [ ] POST user / board / task → 201 ; `A_FAIRE` imposé ; `PATCH` → `EN_COURS`
- [ ] 404 / 400 / 405 / 409 / 400 sur les cas d'erreur
- [ ] Niveau 3 : `\dt` liste `users`, `boards`, `tasks`, et la donnée survit au redémarrage
- [ ] Tests toujours verts avec le profil `postgres` actif

---

## 🩺 Dépannage express

| Symptôme | Cause | Correction |
|----------|-------|------------|
| `release version 17 not supported` | JDK < 17 | Installer/pointer un JDK 17+ (`JAVA_HOME`) — B.7 |
| `./mvn` introuvable | `mvn` ≠ `mvnw` | `.\mvnw.cmd` (PowerShell) / `./mvnw` (Git Bash) |
| `curl : paramètre -X introuvable` | alias PowerShell | `curl.exe` ou `Invoke-RestMethod` |
| `password authentication failed for user "postgres"` | mauvais mot de passe PG | corriger `SPRING_DATASOURCE_PASSWORD` ; **les tests, eux, restent verts** — B.6 |
| `Driver org.h2.Driver claims to not accept jdbcUrl` | URL changée sans le driver | activer le profil `postgres` — B.8 |
| `Connection refused` sur 5432 | PostgreSQL non démarré | démarrer le service Windows `postgresql-x64-*` |
| `Port 8080 was already in use` | ancienne instance | `Ctrl+C` dans l'autre terminal, ou `--server.port=8081` |
| `fatal: Cannot fast-forward to multiple branches` | `pull` ambigu | `git pull --ff-only origin arena/59f43911-board` |

---

## Pour comprendre, pas seulement exécuter

Chaque étape ici correspond à un mécanisme expliqué dans
[`APPRENDRE.md`](APPRENDRE.md) :

- **B.3 / B.5** : ce que vérifient réellement les 16 tests ;
- **B.6** : pourquoi une variable d'environnement peut écraser `application.properties`, et pourquoi les tests y échappent ;
- **B.7** : JDK qui exécute ≠ version cible du bytecode ;
- **B.8** : le profil `postgres`, l'audit de compatibilité du schéma, et la différence entre *application* et *tests* ;
- **Partie C** : 12 exercices, dont le n°10 (reproduire la panne PostgreSQL),
  le n°11 (voir le bytecode changer de version) et le n°12 (basculer sur PostgreSQL).
