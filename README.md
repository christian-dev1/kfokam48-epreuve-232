# PresenceLab — Épreuve finale fullstack KFOKAM48

Présence par code, dépôt d'exercices et relecture entre pairs pour la formation KFOKAM48.
Candidat : Christian Noutchom · matricule 232.

| | |
|---|---|
| Backend | Java 17 · Spring Boot 3.5 · Maven (`mvnw`) · Flyway · PostgreSQL 16 |
| Frontend | **React + Vite** : trois écrans simples ne justifient ni un framework complet ni du rendu serveur, et Vite compile en moins d'une seconde |
| Analyse | [`docs/CAHIER_DES_CHARGES.md`](docs/CAHIER_DES_CHARGES.md) · [`docs/diagrammes/`](docs/diagrammes) · [`api/contrat.yaml`](api/contrat.yaml) · [`docs/JOURNAL.md`](docs/JOURNAL.md) · [`CHANGELOG.md`](CHANGELOG.md) |

## Démarrer

### Option 1 : une commande (Docker)
Prérequis : Docker et Docker Compose.
```bash
docker compose up --build
```
- Frontend : http://localhost:5173
- API : http://localhost:8080 (exemple : http://localhost:8080/api/promotions)
- Le premier build télécharge les dépendances Maven et npm : compter quelques minutes selon la connexion. Les suivants utilisent le cache.
- Pour repartir d'une base vide : `docker compose down -v`.

### Option 2 : trois commandes (sans construire les images)
Prérequis : Java 17+, Node 20+, Docker (pour PostgreSQL seulement).
```bash
docker compose up -d db
(cd backend && FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo ./mvnw spring-boot:run) &
cd frontend && npm install && npm run dev
```

### Données de démonstration
Chargées automatiquement : promotion **« KFOKAM48 - Batch 2 »** (6 étudiants), deux séances passées, des présences (dont une ajoutée par le formateur), des exercices relus deux fois, une fois (note provisoire) ou pas encore relus.

**Parcours conseillé :**
1. Onglet **Formateur** : choisir « KFOKAM48 - Batch 2 » et ouvrir une session (le code s'affiche).
2. Onglet **Étudiant** : choisir Alice, saisir le code ; puis Bruno et Carine. Alice dépose un lien.
3. Onglet **Relecteur** : choisir l'un des deux relecteurs tirés au sort et rendre une note.
4. Onglet **Formateur** : cliquer sur **Actualiser**. La moyenne d'Alice apparaît, marquée « provisoire » tant que le second relecteur n'a pas rendu sa note.

## Tests (sans base locale)
```bash
cd backend && ./mvnw test        # Windows : mvnw.cmd test
```
Les tests tournent sur H2 en mémoire, avec les mêmes migrations Flyway.
- **Unitaires** : règles de présence RG1 à RG4 et course sur la présence (`PresenceServiceTest`), tirage des deux relecteurs (`AssignationRelecteurTest`), validité des liens (`LienExerciceTest`).
- **Intégration** : `POST /api/relectures/{id}` (`RelectureIntegrationTest`), `GET /api/tableau`, 10 présences simultanées (`PresenceConcurrenceIntegrationTest`), format d'erreur 404.

Build du frontend : `cd frontend && npm install && npm run build`.

## Structure
```
api/contrat.yaml      contrat OpenAPI (5 opérations imposées + ajouts)
backend/              Spring Boot : controleur → service → repository, DTO, migrations db/migration, démo db/demo
frontend/             React : src/api/client.js (seul point d'appel HTTP), src/ecrans/ (3 écrans)
docs/                 cahier des charges, journal, diagrammes Mermaid D1–D4
```
