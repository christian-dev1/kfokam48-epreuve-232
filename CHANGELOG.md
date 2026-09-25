# Changelog — PresenceLab

Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Chaque entrée renvoie à son issue GitHub.

## [1.0.0] — 2026-09-25

Version finale, après l'étape 3 (retour du client : un bug et un changement de besoin).

### Corrigé
- **Présences simultanées** : deux requêtes arrivant en même temps pouvaient passer ensemble le contrôle « déjà présent ». La seconde était alors rejetée par la base avec une erreur générique `409 CONFLIT`. L'écriture est désormais immédiate (`saveAndFlush`), et la violation d'unicité est traduite en `409 DEJA_PRESENT`. Le bug est reproduit par un test qui échouait avant le correctif, et un test lance 10 étudiants en simultané (RG3).
- **Build Docker du backend** : l'étape `dependency:go-offline`, silencieuse, semblait bloquée sur une connexion lente. Elle est remplacée par un build avec cache Maven (#1).

### Modifié — changement de besoin « deux relecteurs »
- Chaque exercice est relu par **deux pairs différents** (RG10 v2, qui remplace Q6).
- Note retenue = moyenne des deux notes (RG19). Tant qu'une seule est rendue, la note est affichée comme **provisoire** (RG20) : nouvel état `PARTIELLEMENT_RELU` et champ `moyenneProvisoire` dans le tableau.
- Migration **V2** (V1 inchangée) : la base existante est conservée, et les exercices relus une seule fois deviennent `PARTIELLEMENT_RELU`.
- Contrat d'API **1.2**, cahier des charges **v2** et diagrammes D1, D2 et D4 mis à jour.

### Retiré du périmètre (sacrifice assumé, voir `docs/JOURNAL.md`, étape 3)
- EF12 : blocage deux minutes après cinq codes erronés (Should).
- EF13 : remplacement du lien d'un exercice (Could).

## [0.1.0] — 2026-09-25

Première version : les 8 stories **Must**.

### Ajouté
- Démarrage en une commande (`docker compose up`) avec données de démonstration, migration Flyway V1 et format d'erreur `{ code, message }` centralisé (#1)
- Le formateur ouvre une session et obtient un code valable 15 minutes (#2)
- L'étudiant choisit son nom dans la liste de sa promotion (#3)
- L'étudiant marque sa présence avec le code : 400, 409 et 410 conformes au contrat (#4)
- L'étudiant dépose le lien de son exercice (#5)
- Un relecteur est tiré au hasard parmi les présents, jamais l'auteur (#6)
- Le relecteur rend une note entière de 0 à 20 et un commentaire, définitifs (#7)
- Le formateur consulte le tableau de sa promotion : présences, dépôts, moyenne et relectures en attente (#8)
