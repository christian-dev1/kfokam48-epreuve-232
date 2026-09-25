# D4 — États-transitions : cycle de vie d'un exercice

Valeurs de la colonne `exercice.statut` (migration V1, contrainte `ck_exercice_statut`).

```mermaid
stateDiagram-v2
    [*] --> DEPOSE : POST /api/exercices (EF4)<br/>lien valide (RG7), session non clôturée (RG8)

    DEPOSE --> EN_ATTENTE_RELECTURE : relecteur tiré au sort (EF5, RG11)<br/>au moins un autre étudiant présent
    DEPOSE --> DEPOSE : aucun candidat relecteur (H2)<br/>ou lien remplacé (RG9)

    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : lien remplacé (EF13, RG9)
    EN_ATTENTE_RELECTURE --> RELU : POST /api/relectures/{id}<br/>note entière 0..20 (RG13)

    RELU --> [*]

    note right of EN_ATTENTE_RELECTURE
        Visible dans le tableau :
        relecturesEnAttente du relecteur (RG17)
        exercicesEnAttente de l'auteur (H9)
    end note
    note right of RELU
        Définitif (RG14, C1) :
        second envoi → 409 RELECTURE_DEJA_RENDUE
        remplacement du lien → 409 EXERCICE_DEJA_RELU
    end note
```

| Transition | Déclencheur | Règle | Erreur si interdite |
|---|---|---|---|
| ∅ → DEPOSE | dépôt du lien | RG6, RG7, RG8 | 400 `LIEN_INVALIDE` · 409 `EXERCICE_DEJA_DEPOSE` · 409 `SESSION_CLOTUREE` |
| DEPOSE → EN_ATTENTE_RELECTURE | tirage du relecteur, dans la même transaction que le dépôt | RG10, RG11, RG12 | — (reste DEPOSE s'il n'y a aucun candidat) |
| EN_ATTENTE_RELECTURE → RELU | relecture rendue | RG13, RG14 | 400 `NOTE_INVALIDE` · 403 `AUTO_RELECTURE` · 409 `RELECTURE_DEJA_RENDUE` |
| RELU → (remplacement du lien) | interdit | RG9 | 409 `EXERCICE_DEJA_RELU` |
