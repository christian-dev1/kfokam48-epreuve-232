# PresenceLab — Épreuve finale fullstack KFOKAM48

Application de présence par code, de dépôt d'exercices et de relecture entre pairs, pour la formation KFOKAM48.
Candidat : Christian Noutchom · matricule 232.

| | |
|---|---|
| Backend | Java 17 · Spring Boot 3.5 · Maven (`mvnw`) · Flyway · PostgreSQL 16 |
| Frontend | **React + Vite** — trois écrans simples ne justifient ni un framework complet ni du rendu serveur, et Vite compile en moins d'une seconde |
| Analyse | [`docs/CAHIER_DES_CHARGES.md`](docs/CAHIER_DES_CHARGES.md) · [`docs/diagrammes/`](docs/diagrammes) · [`api/contrat.yaml`](api/contrat.yaml) · [`docs/JOURNAL.md`](docs/JOURNAL.md) |

## Démarrer (une commande)

Prérequis : Docker et Docker Compose.

```bash
docker compose up --build
```

- Frontend : http://localhost:5173
- API : http://localhost:8080 (exemple : http://localhost:8080/api/promotions)
- Données de démonstration chargées automatiquement : promotion « KFOKAM48 - Batch 2 », 6 étudiants, 2 séances, des présences, des exercices et des relectures.

Pour repartir d'une base vide : `docker compose down -v`.

## Lancer les tests (sans base locale)

```bash
cd backend && ./mvnw test        # Windows : mvnw.cmd test
```

Les tests utilisent H2 en mémoire avec les mêmes migrations Flyway.
