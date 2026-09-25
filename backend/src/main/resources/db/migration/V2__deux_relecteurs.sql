-- V2 — Changement de l'étape 3 : chaque exercice est relu par DEUX pairs différents (RG10 v2),
-- note retenue = moyenne (RG19), note provisoire tant qu'une seule est rendue (RG20).
-- V1 n'est pas modifiée. Les données existantes survivent : aucune ligne supprimée.
-- SQL compatible PostgreSQL 16 et H2 2.x (tests).

-- 1. Un exercice peut avoir plusieurs relectures, mais jamais deux fois le même relecteur.
--    La clé étrangère est retirée puis recréée : sous H2, elle réutilisait l'index UNIQUE de
--    uk_relecture_exercice, qui survivait sinon à la suppression de la contrainte.
ALTER TABLE relecture DROP CONSTRAINT fk_relecture_exercice;
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;
ALTER TABLE relecture ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);
ALTER TABLE relecture ADD CONSTRAINT fk_relecture_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id);
CREATE INDEX idx_relecture_exercice ON relecture (exercice_id);

-- 2. Nouvel état du cycle de vie (D4) : une note rendue sur deux = provisoire
ALTER TABLE exercice DROP CONSTRAINT ck_exercice_statut;
ALTER TABLE exercice ADD CONSTRAINT ck_exercice_statut
    CHECK (statut IN ('DEPOSE', 'EN_ATTENTE_RELECTURE', 'PARTIELLEMENT_RELU', 'RELU'));

-- 3. Données existantes (H12) : un exercice « RELU » avec une seule relecture rendue
--    n'a désormais qu'une note provisoire
UPDATE exercice
   SET statut = 'PARTIELLEMENT_RELU'
 WHERE statut = 'RELU'
   AND (SELECT COUNT(*) FROM relecture r WHERE r.exercice_id = exercice.id AND r.rendue_at IS NOT NULL) = 1;
