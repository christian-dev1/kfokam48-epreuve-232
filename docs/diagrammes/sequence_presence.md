sequenceDiagram
autonumber
actor E as Étudiant
participant C as Controller (/api/presences)
participant S as Service
participant DB as Base de Données

    E->>C: POST /api/presences {code, etudiantId}
    C->>S: enregistrerPresence(code, etudiantId)
    
    alt Code inconnu
        S-->>C: CodeInconnuException
        C-->>E: 400 Bad Request {code: "CODE_INCONNU", message: "Code non valide"}
    else Déjà présent
        S-->>C: DejaPresentException
        C-->>E: 409 Conflict {code: "DEJA_PRESENT", message: "Présence déjà enregistrée"}
    else Code expiré
        S-->>C: CodeExpireException
        C-->>E: 410 Gone {code: "CODE_EXPIRE", message: "Le code de présence a expiré"}
    else Succès (Nominal)
        S->>DB: Sauvegarder Présence (source=ETUDIANT)
        DB-->>S: OK
        S-->>C: PresenceDTO
        C-->>E: 201 Created {id, sessionId, etudiantId, source: "ETUDIANT"}
    end