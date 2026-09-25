# D2 — Modèle de données

Ce diagramme correspond **exactement** aux migrations Flyway de `backend/src/main/resources/db/migration/` : mêmes tables, colonnes, types, contraintes. Toute évolution du schéma passe par une nouvelle migration **et** par une mise à jour de ce fichier dans le même commit.

Migrations décrites : `V1__schema_initial.sql`

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION_COURS : "a pour sessions"
    SESSION_COURS ||--o{ PRESENCE : "enregistre"
    ETUDIANT ||--o{ PRESENCE : "est présent (RG3 : 1 par session)"
    SESSION_COURS ||--o{ EXERCICE : "reçoit"
    ETUDIANT ||--o{ EXERCICE : "dépose (RG6 : 1 par session)"
    EXERCICE ||--o| RELECTURE : "est relu par (RG10 : au plus 1)"
    ETUDIANT ||--o{ RELECTURE : "relit (relecteur)"

    PROMOTION {
        bigint id PK
        varchar_100 nom UK "NOT NULL"
    }
    ETUDIANT {
        bigint id PK
        varchar_100 nom "NOT NULL"
        bigint promotion_id FK "NOT NULL"
    }
    SESSION_COURS {
        bigint id PK
        varchar_200 titre "NOT NULL"
        bigint promotion_id FK "NOT NULL"
        varchar_6 code UK "NOT NULL (RG18)"
        timestamptz ouverture_at "NOT NULL"
        timestamptz expiration_at "NOT NULL = ouverture + 15 min (RG1)"
        timestamptz cloture_at "NULL tant que non clôturée"
    }
    PRESENCE {
        bigint id PK
        bigint session_id FK "NOT NULL, UK(session_id, etudiant_id)"
        bigint etudiant_id FK "NOT NULL"
        varchar_10 source "ETUDIANT | FORMATEUR (RG4)"
        timestamptz marquee_at "NOT NULL"
    }
    EXERCICE {
        bigint id PK
        bigint session_id FK "NOT NULL, UK(session_id, etudiant_id)"
        bigint etudiant_id FK "NOT NULL (auteur)"
        varchar_500 lien "NOT NULL (RG7)"
        varchar_30 statut "DEPOSE | EN_ATTENTE_RELECTURE | RELU"
        timestamptz depose_at "NOT NULL"
    }
    RELECTURE {
        bigint id PK
        bigint exercice_id FK "NOT NULL, UK (RG10)"
        bigint relecteur_id FK "NOT NULL, jamais l'auteur (RG12)"
        smallint note "NULL tant que non rendue, CHECK 0..20 (RG13)"
        varchar_1000 commentaire "NULL tant que non rendue"
        timestamptz assignee_at "NOT NULL"
        timestamptz rendue_at "NULL tant que non rendue (RG14)"
    }
```

## Contraintes portées par la base

| Contrainte | Table | Règle |
|---|---|---|
| `uk_promotion_nom` UNIQUE (nom) | promotion | — |
| `uk_session_code` UNIQUE (code) | session_cours | RG18 |
| `uk_presence_session_etudiant` UNIQUE (session_id, etudiant_id) | presence | RG3 |
| `ck_presence_source` CHECK source IN ('ETUDIANT','FORMATEUR') | presence | RG4 |
| `uk_exercice_session_etudiant` UNIQUE (session_id, etudiant_id) | exercice | RG6 |
| `ck_exercice_statut` CHECK statut IN ('DEPOSE','EN_ATTENTE_RELECTURE','RELU') | exercice | D4 |
| `uk_relecture_exercice` UNIQUE (exercice_id) | relecture | RG10 |
| `ck_relecture_note` CHECK note BETWEEN 0 AND 20 | relecture | RG13 |

Le **relecteur** n'a pas de table propre : c'est un `ETUDIANT` référencé par `relecture.relecteur_id` (cahier des charges, section 2). Le **formateur** n'est pas stocké, puisqu'il n'y a pas d'authentification (Q1, section 3).
