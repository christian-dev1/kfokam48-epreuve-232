# D3 — Séquence : marquer sa présence

Opération imposée : `POST /api/presences` (contrat). L'ordre des vérifications suit l'hypothèse H6 du cahier des charges : **blocage → code inconnu → code expiré ou session clôturée → déjà présent → succès**. Chaque erreur renvoie le format imposé `{ code, message }`, produit par le `@RestControllerAdvice` unique (`GestionErreurs`).

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Front (EcranEtudiant)
    participant API as PresenceController
    participant S as PresenceService
    participant DB as Base (Repositories)
    participant ADV as GestionErreurs<br/>(@RestControllerAdvice)

    E->>F: choisit son nom (EF2) puis saisit le code
    F->>API: POST /api/presences { code, etudiantId }
    API->>API: validation du DTO (@Valid)
    alt champ manquant
        API-->>ADV: MethodArgumentNotValidException
        ADV-->>F: 400 { code: "CHAMP_MANQUANT", message }
    end
    API->>S: marquerPresence(code, etudiantId)

    alt étudiant bloqué après 5 codes erronés (RG5) [Should, EF12]
        S-->>ADV: TropDeTentativesException
        ADV-->>F: 429 { code: "TROP_DE_TENTATIVES", message }
    else code inconnu
        S->>DB: findByCode(code)
        DB-->>S: vide
        S->>DB: incrémente le compteur d'échecs (RG5)
        S-->>ADV: CodeInconnuException
        ADV-->>F: 400 { code: "CODE_INCONNU", message }
    else code expiré ou session clôturée (RG1, RG2)
        S->>DB: findByCode(code)
        DB-->>S: SessionCours (maintenant > expirationAt ou clotureAt renseigné)
        S-->>ADV: CodeExpireException
        ADV-->>F: 410 { code: "CODE_EXPIRE", message: "Le code de présence a expiré." }
    else étudiant déjà présent (RG3)
        S->>DB: existsBySessionIdAndEtudiantId(sessionId, etudiantId)
        DB-->>S: true
        S-->>ADV: DejaPresentException
        ADV-->>F: 409 { code: "DEJA_PRESENT", message }
    else cas nominal
        S->>DB: findByCode(code)
        DB-->>S: SessionCours valide
        S->>DB: save(Presence source=ETUDIANT) (RG4)
        DB-->>S: Presence
        S-->>API: PresenceDto
        API-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
    end
    F-->>E: confirmation ou message d'erreur lisible
```

| Cas | Code HTTP | `code` d'erreur | Règle | Dans le contrat |
|---|---|---|---|---|
| Présence enregistrée | 201 | — | RG4 | oui |
| Champ manquant | 400 | `CHAMP_MANQUANT` | — | oui (400) |
| Code inconnu | 400 | `CODE_INCONNU` | RG5 | oui |
| Étudiant inconnu | 400 | `ETUDIANT_INCONNU` | — | ajouté (400) |
| Étudiant d'une autre promotion | 400 | `ETUDIANT_HORS_PROMOTION` | H7 | ajouté (400) |
| Déjà présent | 409 | `DEJA_PRESENT` | RG3 | oui |
| Code expiré / session clôturée | 410 | `CODE_EXPIRE` | RG1, RG2 | oui |
| Trop de tentatives | 429 | `TROP_DE_TENTATIVES` | RG5 | ajouté (Should) |
