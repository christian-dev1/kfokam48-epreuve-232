# Cahier des charges — PresenceLab KFOKAM48

**Auteur :** Christian Noutchom · matricule 232
**Version :** 1 · **Date :** 25/09/2026
**Frontend choisi :** React (Vite), parce que trois écrans simples n'ont besoin ni d'un framework complet ni de rendu serveur, et que le build Vite est rapide et sans configuration.

---

## 1. Contexte et objectif

La formation KFOKAM48 suit aujourd'hui les présences, les exercices et les relectures entre pairs à la main (feuille d'appel, liens envoyés par messagerie, notes recopiées). Cela pose trois problèmes : la présence n'est pas fiable (on signe pour un absent), les liens d'exercices se perdent, et le formateur n'a aucune vue d'ensemble de sa promotion.

L'application **PresenceLab** remplace ce processus par un flux unique :
le formateur ouvre une session et affiche un code, les étudiants présents saisissent ce code, déposent le lien de leur exercice, le système désigne un pair pour relire chaque exercice, et le formateur retrouve dans un tableau, par étudiant, ses présences, ses dépôts, la moyenne des notes reçues et les relectures qu'il doit encore rendre.

**Objectif mesurable :** à la fin d'une séance, le formateur obtient le tableau de sa promotion sans aucune saisie manuelle, sauf les présences qu'il ajoute lui-même, qui restent identifiables.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session et obtenir son code · ajouter une présence à la main · clôturer une session · consulter le tableau d'une promotion | Noter un exercice · voir qui a relu qui depuis l'écran étudiant |
| **Étudiant** | Choisir son nom dans la liste de sa promotion · marquer sa présence avec le code · déposer (et remplacer) le lien de son exercice · voir la note et le commentaire reçus | Marquer sa présence après expiration du code ou clôture · déposer deux exercices pour la même session · connaître le nom de son relecteur |
| **Relecteur** | Voir les exercices qui lui sont assignés · rendre une note entière de 0 à 20 et un commentaire | Relire son propre exercice · choisir l'exercice qu'il relit · modifier une relecture déjà rendue |

**Décision : le relecteur n'est pas un acteur distinct, c'est un étudiant dans un certain état** (celui à qui une relecture a été assignée). Conséquence sur le modèle de données : il n'y a pas de table `relecteur`. La table `relecture` porte une clé étrangère `relecteur_id` vers `etudiant`. Un même étudiant est à la fois auteur de ses exercices et relecteur de ceux des autres.

Le **Système** est un acteur secondaire : il génère le code de présence et tire le relecteur au sort.

## 3. Périmètre

**Inclus dans cette version :**
- Ouverture d'une session avec code de présence à durée limitée
- Sélection de son nom dans une liste (pas d'authentification, voir Q1)
- Présence par code, et présence ajoutée par le formateur (marquée `FORMATEUR`)
- Dépôt d'un lien d'exercice par session, avec remplacement possible
- Assignation automatique et aléatoire d'un relecteur parmi les présents
- Relecture : note entière sur 20 et commentaire
- Tableau du formateur par promotion
- Clôture d'une session par le formateur
- Données de démonstration chargées au démarrage

**Explicitement exclu :**
- Authentification, mots de passe, gestion des droits (Q1). N'importe qui peut ouvrir l'écran formateur : c'est un risque accepté pour cette version.
- Gestion (création, modification, suppression) des promotions et des étudiants : ils sont fournis par les données de démonstration
- Modification d'une note après envoi (voir la contradiction Q10/Q15, section 7)
- Relance ou réassignation d'un relecteur défaillant (Q11 demande seulement de le voir)
- Notifications (e-mail, SMS), export PDF/Excel, historique détaillé par session dans le tableau
- Upload de fichiers : seul un lien est déposé
- Application mobile native : l'interface web doit simplement être utilisable sur téléphone

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session pour une promotion et obtient un code de présence | Quand j'envoie un titre et une promotion existante, alors je reçois `201` avec un code de 6 caractères, `ouvertureAt` et `expirationAt` = ouverture + 15 min · quand un champ manque, alors `400` | Must |
| EF2 | L'étudiant s'identifie en choisissant son nom dans la liste de sa promotion | Quand je choisis une promotion, alors la liste de ses étudiants s'affiche et je peux sélectionner mon nom sans mot de passe | Must |
| EF3 | L'étudiant marque sa présence avec le code | Quand je saisis un code valide et non expiré, alors ma présence (source `ETUDIANT`) apparaît dans le tableau · code inconnu → `400 CODE_INCONNU` · code expiré ou session clôturée → `410 CODE_EXPIRE` · déjà présent → `409 DEJA_PRESENT` | Must |
| EF4 | L'étudiant dépose le lien de son exercice pour une session | Quand j'envoie un lien http(s) valide, alors je reçois `201 { id, statut }` · lien invalide → `400 LIEN_INVALIDE` · second dépôt pour la même session → `409 EXERCICE_DEJA_DEPOSE` · session clôturée → `409 SESSION_CLOTUREE` | Must |
| EF5 | Le système désigne un relecteur au hasard pour chaque exercice déposé | Quand un exercice est déposé et qu'au moins un autre étudiant est présent à la session, alors une relecture lui est assignée et l'exercice passe `EN_ATTENTE_RELECTURE` · l'auteur n'est jamais tiré · s'il n'y a aucun candidat, l'exercice reste `DEPOSE` | Must |
| EF6 | Le relecteur voit la liste des exercices qu'il doit relire | Quand je sélectionne mon nom sur l'écran relecteur, alors je vois mes relectures à faire avec le lien de l'exercice, sans le nom de l'auteur | Must |
| EF7 | Le relecteur rend une note et un commentaire | Quand j'envoie une note entière entre 0 et 20 et un commentaire, alors je reçois `200` et l'exercice passe `RELU` · note hors bornes ou décimale → `400 NOTE_INVALIDE` · relecture de mon propre exercice → `403 AUTO_RELECTURE` · relecture déjà rendue → `409 RELECTURE_DEJA_RENDUE` | Must |
| EF8 | Le formateur consulte le tableau de sa promotion | Quand je choisis une promotion, alors je vois une ligne par étudiant avec `presences`, `exercicesDeposes`, `moyenne` (fournie par l'API, vide si aucune note) et `relecturesEnAttente` · promotion inconnue → `404 PROMOTION_INCONNUE` | Must |
| EF9 | Le formateur ajoute une présence à la main | Quand j'ajoute un étudiant à une session non clôturée, alors sa présence est créée avec la source `FORMATEUR` et elle est signalée « ajoutée par le formateur » · déjà présent → `409 DEJA_PRESENT` | Should |
| EF10 | Le formateur clôture une session | Quand je clôture une session, alors aucun dépôt ni présence n'est plus accepté pour elle (`409 SESSION_CLOTUREE` au dépôt) · clôturer deux fois → `409 SESSION_DEJA_CLOTUREE` | Should |
| EF11 | L'étudiant consulte la note et le commentaire reçus | Quand j'ouvre mes exercices, alors je vois le statut, la note et le commentaire, et jamais le nom du relecteur | Should |
| EF12 | Un étudiant est bloqué deux minutes après cinq codes erronés | Quand je saisis 5 codes inconnus d'affilée, alors toute nouvelle tentative pendant 2 minutes renvoie `429 TROP_DE_TENTATIVES`, même avec un code valide | Should |
| EF13 | L'étudiant remplace le lien de son exercice | Quand la relecture de mon exercice n'est pas encore rendue, alors je peux remplacer le lien (`200`) · sinon → `409 EXERCICE_DEJA_RELU` | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'écran étudiant (présence, dépôt) est utilisable sur un téléphone (largeur 360 px) | Ouvrir l'écran en mode responsive 360 px dans le navigateur : pas de défilement horizontal, champs et boutons utilisables au doigt |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants et 30 sessions | Le tableau est calculé par des requêtes agrégées (pas de N+1) ; mesure dans l'onglet Réseau du navigateur sur les données de démo |
| ENF3 | Volumétrie cible : 5 promotions, 60 étudiants par promotion, 1 session par jour, 60 marquages de présence dans la même minute | Contrainte d'unicité en base sur (session, étudiant) ; test de concurrence sur le marquage de présence |
| ENF4 | L'application démarre chez un tiers en une commande, avec des données de démonstration | `docker compose up` depuis un clone vierge ; le tableau de la promotion de démo n'est pas vide |
| ENF5 | Toute erreur renvoie le JSON `{ code, message }`, jamais une stack trace ni la page d'erreur Spring | Test d'intégration sur un cas d'erreur ; appel d'une URL inexistante → `404` au format imposé |
| ENF6 | Les interfaces affichent un état de chargement et un message d'erreur lisible | Couper le backend puis utiliser un écran : un message d'erreur s'affiche, pas de page blanche |
| ENF7 | Le code de présence n'est pas devinable facilement | 6 caractères parmi 32 (lettres majuscules et chiffres, sans 0/O/1/I), soit environ 10⁹ combinaisons, tirés par `SecureRandom`, et blocage EF12 |
| ENF8 | Les dates sont stockées en UTC et renvoyées au format ISO 8601 | `ouvertureAt` et `expirationAt` au format `2026-09-25T09:00:00Z` dans la réponse |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session (`expirationAt = ouvertureAt + 15 min`) | Q2 |
| RG2 | Aucune présence par code n'est acceptée après expiration du code ou après clôture de la session → `410 CODE_EXPIRE` | Q2, Q3 |
| RG3 | Un étudiant a au plus une présence par session, quelle que soit sa source → `409 DEJA_PRESENT` | Q14, contrat |
| RG4 | Une présence ajoutée par le formateur porte la source `FORMATEUR` ; une présence par code porte la source `ETUDIANT` | Q14 |
| RG5 | Après 5 codes erronés consécutifs, l'étudiant est bloqué 2 minutes ; un code correct remet le compteur à zéro | Q4 |
| RG6 | Un étudiant dépose au plus un exercice par session → `409 EXERCICE_DEJA_DEPOSE` | Contrat |
| RG7 | Le lien d'un exercice est une URL absolue en `http` ou `https`, de 500 caractères au plus → sinon `400 LIEN_INVALIDE` | Contrat |
| RG8 | Le dépôt est possible tant que la session n'est pas clôturée, même après la fin du cours → sinon `409 SESSION_CLOTUREE` | Q12 |
| RG9 | Le lien peut être remplacé tant que la relecture de l'exercice n'est pas rendue | Q13, hypothèse H4 |
| RG10 | Chaque exercice a exactement un relecteur | Q6 |
| RG11 | Le relecteur est tiré au hasard parmi les étudiants présents à la session de l'exercice, auteur exclu ; à tirage égal, on privilégie celui qui a le moins de relectures en cours | Q7, H2 |
| RG12 | Un étudiant ne relit jamais son propre exercice → `403 AUTO_RELECTURE` | Q5 |
| RG13 | Une note est un entier compris entre 0 et 20 inclus → sinon `400 NOTE_INVALIDE` | Q9 |
| RG14 | Une relecture rendue est définitive : un second envoi renvoie `409 RELECTURE_DEJA_RENDUE` | Q15, contrat (C1) |
| RG15 | L'auteur voit la note et le commentaire reçus, jamais l'identité du relecteur | Q8 |
| RG16 | La moyenne d'un étudiant est la moyenne arithmétique des notes des relectures **rendues** sur ses exercices, arrondie à 2 décimales ; elle vaut `null` s'il n'a reçu aucune note | Q16 |
| RG17 | `relecturesEnAttente` d'un étudiant = nombre de relectures qui lui sont assignées et pas encore rendues | Q11, Q16 |
| RG18 | Le code d'une session est unique parmi toutes les sessions | ENF7 |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

| # | Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|---|
| H1 | **Le trou : d'où vient la liste des étudiants ?** Q1 dit « l'étudiant choisit son nom dans une liste », mais personne n'a dit qui crée les promotions et les étudiants | Aucune réponse | Promotions et étudiants sont chargés par les données de démonstration ; leur gestion est hors périmètre. On ajoute deux opérations de lecture au contrat | `GET /api/promotions`, `GET /api/promotions/{id}/etudiants` |
| H2 | **Et si aucun autre étudiant n'est présent ?** Q7 impose un relecteur « présent à la session », mais l'auteur peut être le seul présent, ou déposer sans avoir été présent (Q12) | Aucune réponse | Le relecteur est tiré au dépôt. S'il n'y a aucun candidat, l'exercice reste `DEPOSE`, sans relecteur, et reste visible comme non relu | Statut `DEPOSE` distinct de `EN_ATTENTE_RELECTURE` (diagramme D4) |
| H3 | **Qu'est-ce que « la fin de la session » (Q3, Q12) ?** Une session n'a pas d'heure de fin, seulement une ouverture, une expiration du code (Q2) et une clôture par le formateur (Q10, Q12) | Q3, Q12 | « Fin de session » = **clôture par le formateur**. Pour la présence par code, l'expiration à 15 min arrive toujours avant, donc Q3 est satisfaite | Champ `cloture_at` sur la session ; opération `POST /api/sessions/{id}/cloture` |
| H4 | **« Tant que personne n'a commencé à le relire » (Q13)** : on ne peut pas savoir quand quelqu'un « commence » à lire un lien | Q13 | On considère qu'une relecture est commencée quand elle est rendue : le lien reste remplaçable tant que la note n'est pas envoyée | RG9 |
| H5 | **Qui envoie la relecture ?** Sans authentification, le contrat `POST /api/relectures/{id}` ne dit pas qui appelle, alors qu'il prévoit un `403` d'auto-relecture | Contrat | Le corps accepte un champ `relecteurId` en plus. S'il vaut l'auteur de l'exercice → `403 AUTO_RELECTURE` ; s'il diffère du relecteur assigné → `403 RELECTEUR_NON_ASSIGNE`. Le tirage exclut déjà l'auteur (double protection) | Champ facultatif ajouté au contrat, les champs imposés sont inchangés |
| H6 | **L'ordre des vérifications** sur `POST /api/presences` quand plusieurs erreurs s'appliquent en même temps | Contrat | Blocage (`429`) → code inconnu (`400`) → code expiré ou session clôturée (`410`) → déjà présent (`409`) | Diagramme D3 |
| H7 | **Une présence par code doit-elle venir d'un étudiant de la promotion de la session ?** | Aucune réponse | Oui : un étudiant d'une autre promotion reçoit `400 ETUDIANT_HORS_PROMOTION` | Contrôle dans le service |
| H8 | **Q16 demande « sa présence à chaque session »**, alors que le contrat impose `presences` sous forme d'entier | Q16, contrat | Le contrat prime : `presences` = nombre de sessions où l'étudiant est présent. Le détail par session est exclu de cette version | Section 3, exclusions |
| H9 | **Q11 veut voir « clairement » un exercice en attente dans le tableau**, alors que le contrat n'a pas de champ pour cela | Q11 | On ajoute au tableau un champ **supplémentaire** `exercicesEnAttente` (exercices déposés sans relecture rendue). Les six champs imposés sont inchangés | Contrat complété |
| H10 | **Q4 : qu'est-ce qu'« une erreur » ?** | Q4 | Seul un code **inconnu** compte comme une erreur. Un code expiré ou un « déjà présent » ne compte pas : l'étudiant ne devinait pas | RG5 |

**Contradictions relevées :**

| # | Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|---|
| C1 | **Q10** (« le relecteur peut corriger sa note tant que la session n'est pas clôturée ») contre **Q15** (« une fois validée, c'est fini, il ne peut plus y revenir ») | **Q15 : une relecture rendue est définitive** (RG14) | Le contrat imposé tranche : `POST /api/relectures/{id}` doit renvoyer `409 RELECTURE_DEJA_RENDUE` sur un second envoi, ce qui n'a de sens que si la note est définitive. Le contrat étant non négociable (B2), Q10 ne peut pas être satisfaite sans le violer. Q15 donne en plus une raison métier (« plus honnête pour tout le monde »), alors que Q10 n'en donne pas |
| C2 | **Q13** (« remplacer le lien tant que personne n'a commencé à relire ») contre la **note définitive (Q15)**, tension mineure : si l'auteur change le lien après la relecture, la note porterait sur un autre travail | Remplacement autorisé uniquement avant la relecture rendue (RG9, H4) | Cela préserve le sens de Q13 sans rendre Q15 incohérente |

**Réponses jugées sans impact :** Q1 ne demande aucun développement (il ne fait qu'exclure l'authentification) et Q8 n'impose qu'une restriction d'affichage.

## 8. Contraintes techniques

**Imposées par le sujet :**
- **B1** Java 17, Spring Boot 3.5, **Maven** avec wrapper `mvnw` commité
- **B2** Le contrat `api/contrat.yaml` est respecté à la lettre : chemins, verbes, codes, format d'erreur `{ code, message }`
- **B3** Couches contrôleur / service / repository, DTO en entrée et en sortie, aucune entité JPA exposée
- **B4** Bean Validation sur les DTO d'entrée, `@RestControllerAdvice` unique qui traduit toutes les exceptions au format imposé
- **B5** Schéma versionné par **Flyway** (`backend/src/main/resources/db/migration`), `ddl-auto=validate`. Une migration commitée n'est jamais modifiée : toute évolution passe par une nouvelle migration
- **B6** Un test unitaire (JUnit 5 + Mockito) sur une règle métier (RG1/RG2/RG3 au marquage de présence) et un test d'intégration (`@SpringBootTest` + MockMvc) sur un endpoint, exécutés sur **H2 en mémoire** : `./mvnw test` passe sans base locale
- **F1** React + Vite, justifié dans le README, `npm run build` passe
- **F2** Trois écrans : Formateur, Étudiant, Relecteur
- **F3** Tous les appels HTTP passent par `frontend/src/api/client.js` · états chargement/erreur sur chaque écran · la moyenne vient de l'API
- **Démarrage** : `docker compose up` (PostgreSQL + backend + frontend) avec des données de démonstration

**Que je m'impose :**
- **PostgreSQL 16** en exécution, H2 (mode PostgreSQL) en test
- Données de démonstration dans un emplacement Flyway séparé (`db/demo`), activé seulement au démarrage Docker, pour ne pas polluer les tests
- Unicité garantie **en base** (contraintes `UNIQUE`), pas seulement dans le code, pour résister aux accès concurrents
- `spring.jackson.deserialization.accept-float-as-int=false` pour qu'une note `12.5` soit refusée (`400`) au lieu d'être tronquée en `12`
- Git : `main` protégé par convention, une branche `feature/<n>-<sujet>` ou `fix/<n>-<sujet>` par issue, fusion par PR, messages de commit de la forme `type(portée): résumé (RGx) — Closes #n`

## 9. Livrables

- `docs/CAHIER_DES_CHARGES.md` : ce document, tenu à jour après l'étape 3
- `docs/JOURNAL.md` : une entrée par étape
- `docs/diagrammes/` : D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 états d'un exercice (bonus), en Mermaid
- `api/contrat.yaml` : les 5 opérations imposées et les opérations ajoutées
- Backlog en issues GitHub, avec les labels de priorité `Must`, `Should` et `Could`
- `backend/` : API Spring Boot, migrations Flyway, tests
- `frontend/` : application React, 3 écrans
- `docker-compose.yml`, `README.md` (installation testée depuis un clone vierge), `CHANGELOG.md`
- Trois commits `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`
- `SOUMISSION.md` téléversé sur la plateforme

## 10. Démarche prévue

1. **Étape 1 — Analyse (fin visée 14h45).** Cahier des charges, diagrammes, contrat complété et backlog d'issues **avant toute ligne de code**. Le contrat est figé à cette étape.
2. **Étape 2 — v0.1 (fin visée 16h30).** Uniquement les issues **Must**, dans l'ordre des dépendances : démarrage (socle, migration V1, données de démo) → session → liste des étudiants → présence → dépôt et assignation → relecture → tableau → écrans. Une branche et une PR par issue, fusion dans `main` seulement si les tests passent. Puis `[JALON] v0.1`.
3. **Étape 3 — Enveloppe (fin visée 17h20).** Pour un bug : issue, test qui échoue, correctif sur une branche `fix/`. Pour un changement : mise à jour de l'analyse, nouvelle migration (jamais de modification d'une migration existante), contrat, puis code sur une branche `feature/`. Re-priorisation écrite.
4. **Étape 4 — v1.0 (fin visée 17h40).** Issues Should si le temps le permet, `CHANGELOG.md`, README testé depuis un clone vierge, backlog trié, `[JALON] v1.0`.
5. **Étape 5 — Soumission avant 17h45**, pas à 17h58.

**En cas de retard :** les Should et les Could sortent du périmètre dans cet ordre : EF13, EF12, EF11, EF10, EF9. On ne sacrifie jamais les tests B6 ni le format d'erreur.

**Definition of Done — une issue est terminée quand :**
- tous ses critères d'acceptation sont vérifiés, à la main ou par un test
- le code est sur une branche dédiée, fusionnée dans `main` par une PR qui référence l'issue (`Closes #n`)
- `./mvnw test` et `npm run build` passent sur `main` après la fusion
- toute modification du schéma passe par une nouvelle migration Flyway, et D2 est mis à jour si le modèle change
- le contrat `api/contrat.yaml` décrit exactement ce que l'endpoint renvoie, y compris les erreurs

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25/09/2026 | Version initiale |
