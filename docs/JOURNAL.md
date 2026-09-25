# Journal de bord — Christian Noutchom · 232

> Une entrée par étape, écrite au moment où elle se termine.
> Chaque entrée répond à trois questions : **Fait**, **Bloqué**, **IA**.

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges v1 (13 EF, 8 ENF, 18 RG, 10 hypothèses, 2 contradictions), quatre diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence présence, D4 états d'un exercice en bonus), contrat complété avec 6 opérations et le catalogue des codes d'erreur, 13 issues créées avec les labels Must/Should/Could, `.gitignore` Java + JS posé et `.idea/` retiré du suivi.

**Bloqué :** environ 30 min. J'avais posé `[JALON] analyse` trop tôt, alors que le cahier des charges, D2, le contrat complété et les issues n'existaient pas encore. Le commit « docs: finalisation cahier des charges… » n'ajoutait en réalité que `.idea/` et deux diagrammes. Je n'ai pas réécrit l'historique (pas de `push --force` sur `main`) : l'analyse est complétée dans les commits qui suivent le jalon, **avant tout commit de code**, et je le signale ici. Le commit de départ porte le préfixe `[JALON] depart`, comme le demandait la première version du LISEZ-MOI ; la version 2 réserve ce préfixe aux trois jalons. 15 min aussi sur Q10/Q15, tranchée en faveur de Q15 parce que le contrat impose `409 RELECTURE_DEJA_RENDUE`. Enfin, j'ai repéré le trou H1 : personne ne dit d'où vient la liste des étudiants de Q1.

**IA :** je lui ai fait relire tout le sujet et proposer les EF/RG, les diagrammes et les ajouts au contrat. Vérifications : chaque RG renvoie à une question Qx ou au contrat, les quatre diagrammes ont été rendus avec mermaid-cli (aucune erreur de syntaxe), `api/contrat.yaml` passe `openapi-spec-validator`, et chaque code HTTP de D3 a été comparé à la ligne correspondante du contrat. J'ai refusé des tickets techniques (« configurer Flyway ») au profit de titres qui décrivent un résultat.

---

## Étape 2 — Première version (v0.1)

**Fait :** les 8 issues Must (#1 à #8), chacune sur sa branche avec sa PR fusionnée dans `main` : démarrage `docker compose` avec données de démo, migration Flyway V1, gestion centralisée des erreurs, liste des étudiants, ouverture de session, présence par code, dépôt d'exercice, tirage du relecteur, relecture, tableau. Les trois écrans sont en place. Tests : 1 test unitaire des règles de présence (RG1 à RG4), 1 test unitaire du tirage (RG11, RG12), 1 test paramétré des liens (RG7), des tests d'intégration MockMvc sur `POST /api/relectures/{id}` et `GET /api/tableau`, et le 404 au format imposé.

**Bloqué :** environ 25 min : générer le wrapper `mvnw` en Maven, puisque le squelette initial était en Gradle, et fixer les fins de ligne de `mvnw` avec `.gitattributes`. J'ai aussi dû réordonner deux issues : la #3 (liste des promotions) avant la #2, parce que l'écran d'ouverture de session a besoin de choisir une promotion.

**IA :** je lui ai fait écrire le code branche par branche à partir des issues et du contrat. Vérifications : `./mvnw test` vert avant chaque PR (le script d'ouverture de PR bloque sinon) ; comparaison de chaque code HTTP renvoyé avec `api/contrat.yaml` ; la migration V1 comparée colonne par colonne à D2 ; une note de `12.5` testée pour vérifier qu'elle est refusée et non tronquée ; `docker compose up` lancé depuis un clone vierge, avec un parcours complet (ouvrir une session, marquer deux présences, déposer, relire, lire le tableau).
