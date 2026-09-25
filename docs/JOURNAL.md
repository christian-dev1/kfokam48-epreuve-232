# Journal de bord — Christian Noutchom · 232

> Une entrée par étape, écrite au moment où elle se termine.
> Chaque entrée répond à trois questions : **Fait**, **Bloqué**, **IA**.

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges v1 (13 EF, 8 ENF, 18 RG, 10 hypothèses, 2 contradictions), quatre diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence présence, D4 états d'un exercice en bonus), contrat complété avec 6 opérations et le catalogue des codes d'erreur, 13 issues créées avec les labels Must/Should/Could, `.gitignore` Java + JS posé et `.idea/` retiré du suivi.

**Bloqué :** environ 30 min. J'avais posé `[JALON] analyse` trop tôt, alors que le cahier des charges, D2, le contrat complété et les issues n'existaient pas encore. Le commit « docs: finalisation cahier des charges… » n'ajoutait en réalité que `.idea/` et deux diagrammes. Je n'ai pas réécrit l'historique (pas de `push --force` sur `main`) : l'analyse est complétée dans les commits qui suivent le jalon, **avant tout commit de code**, et je le signale ici. Le commit de départ porte le préfixe `[JALON] depart`, comme le demandait la première version du LISEZ-MOI ; la version 2 réserve ce préfixe aux trois jalons. 15 min aussi sur Q10/Q15, tranchée en faveur de Q15 parce que le contrat impose `409 RELECTURE_DEJA_RENDUE`. Enfin, j'ai repéré le trou H1 : personne ne dit d'où vient la liste des étudiants de Q1.

**IA :** je lui ai fait relire tout le sujet et proposer les EF/RG, les diagrammes et les ajouts au contrat. Vérifications : chaque RG renvoie à une question Qx ou au contrat, les quatre diagrammes ont été rendus avec mermaid-cli (aucune erreur de syntaxe), `api/contrat.yaml` passe `openapi-spec-validator`, et chaque code HTTP de D3 a été comparé à la ligne correspondante du contrat. J'ai refusé des tickets techniques (« configurer Flyway ») au profit de titres qui décrivent un résultat.
