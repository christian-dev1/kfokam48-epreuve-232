# D1 — Cas d'utilisation

Le relecteur n'est pas un acteur distinct : c'est un **étudiant** à qui une relecture a été assignée (voir la section 2 du cahier des charges). Il est représenté à part pour la lisibilité, relié à l'étudiant par une généralisation. Le **Système** est un acteur secondaire : il tire les relecteurs au sort.

```mermaid
flowchart LR
    F(["👤 Formateur"])
    E(["👤 Étudiant"])
    R(["👤 Relecteur"])
    SYS(["⚙️ Système"])

    R -. "est un" .-> E

    subgraph APP["PresenceLab"]
        UC1(["UC1 · Ouvrir une session et obtenir un code<br/>EF1 · POST /api/sessions"])
        UC2(["UC2 · Ajouter une présence à la main<br/>EF9 · source FORMATEUR"])
        UC3(["UC3 · Clôturer une session<br/>EF10"])
        UC4(["UC4 · Consulter le tableau de la promotion<br/>EF8 · GET /api/tableau"])
        UC5(["UC5 · Choisir son nom dans la liste<br/>EF2"])
        UC6(["UC6 · Marquer sa présence avec le code<br/>EF3 · POST /api/presences"])
        UC7(["UC7 · Déposer le lien de son exercice<br/>EF4 · POST /api/exercices"])
        UC8(["UC8 · Remplacer le lien de son exercice<br/>EF13 · hors périmètre v2"])
        UC9(["UC9 · Consulter la note reçue<br/>EF11"])
        UC10(["UC10 · Désigner deux relecteurs au hasard (v2)<br/>EF5"])
        UC11(["UC11 · Voir ses relectures à faire<br/>EF6"])
        UC12(["UC12 · Rendre une note et un commentaire<br/>EF7 · POST /api/relectures/{id}"])
    end

    F --- UC1
    F --- UC2
    F --- UC3
    F --- UC4

    E --- UC5
    E --- UC6
    E --- UC7
    E --- UC8
    E --- UC9

    R --- UC11
    R --- UC12

    UC7 -. "«include»" .-> UC10
    UC10 --- SYS
    UC6 -. "«include»" .-> UC5
    UC7 -. "«include»" .-> UC5
    UC11 -. "«include»" .-> UC5
```

| Cas | Priorité | Règles de gestion |
|---|---|---|
| UC1 | Must | RG1, RG18 |
| UC2 | Should | RG3, RG4 |
| UC3 | Should | RG2, RG8 |
| UC4 | Must | RG16, RG17 |
| UC5 | Must | — (Q1) |
| UC6 | Must | RG1, RG2, RG3, RG5 |
| UC7 | Must | RG6, RG7, RG8 |
| UC8 | Could → **sacrifié en v2** | RG9 |
| UC9 | Should | RG15 |
| UC10 | Must | RG10 (v2 : deux relecteurs), RG11, RG12 |
| UC11 | Must | RG15 |
| UC12 | Must | RG12, RG13, RG14 |
