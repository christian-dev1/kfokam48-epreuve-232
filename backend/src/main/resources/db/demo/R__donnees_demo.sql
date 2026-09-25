-- Données de démonstration (ENF4) — chargées uniquement quand FLYWAY_LOCATIONS inclut classpath:db/demo
-- (c'est le cas avec docker compose). Migration « repeatable » et IDEMPOTENTE : chaque insertion est gardée
-- par NOT EXISTS sur une clé naturelle, pour survivre aux migrations suivantes sans doublon.
-- Script PostgreSQL (les tests H2 ne le chargent pas).

-- Promotions
INSERT INTO promotion (nom) SELECT 'KFOKAM48 - Batch 2' WHERE NOT EXISTS (SELECT 1 FROM promotion WHERE nom = 'KFOKAM48 - Batch 2');
INSERT INTO promotion (nom) SELECT 'KFOKAM48 - Batch 1' WHERE NOT EXISTS (SELECT 1 FROM promotion WHERE nom = 'KFOKAM48 - Batch 1');

-- Étudiants
INSERT INTO etudiant (nom, promotion_id)
SELECT v.nom, p.id
FROM (VALUES ('Alice Mbarga', 'KFOKAM48 - Batch 2'),
             ('Bruno Tchoupo', 'KFOKAM48 - Batch 2'),
             ('Carine Ngo', 'KFOKAM48 - Batch 2'),
             ('David Essomba', 'KFOKAM48 - Batch 2'),
             ('Estelle Fotso', 'KFOKAM48 - Batch 2'),
             ('Franck Nana', 'KFOKAM48 - Batch 2'),
             ('Gaelle Kamga', 'KFOKAM48 - Batch 1'),
             ('Hugo Mvondo', 'KFOKAM48 - Batch 1')) AS v(nom, promotion)
JOIN promotion p ON p.nom = v.promotion
WHERE NOT EXISTS (SELECT 1 FROM etudiant e WHERE e.nom = v.nom AND e.promotion_id = p.id);

-- Sessions : une séance clôturée il y a 2 jours, une séance d'hier encore ouverte aux dépôts (Q12)
INSERT INTO session_cours (titre, promotion_id, code, ouverture_at, expiration_at, cloture_at)
SELECT 'Séance 1 - Git et GitHub', p.id, 'DEMO1A',
       now() - INTERVAL '3 days', now() - INTERVAL '3 days' + INTERVAL '15 minutes', now() - INTERVAL '2 days'
FROM promotion p WHERE p.nom = 'KFOKAM48 - Batch 2'
  AND NOT EXISTS (SELECT 1 FROM session_cours WHERE code = 'DEMO1A');

INSERT INTO session_cours (titre, promotion_id, code, ouverture_at, expiration_at, cloture_at)
SELECT 'Séance 2 - API REST Spring', p.id, 'DEMO2B',
       now() - INTERVAL '1 day', now() - INTERVAL '1 day' + INTERVAL '15 minutes', NULL
FROM promotion p WHERE p.nom = 'KFOKAM48 - Batch 2'
  AND NOT EXISTS (SELECT 1 FROM session_cours WHERE code = 'DEMO2B');

-- Présences (Franck ajouté par le formateur en séance 1 : Q14)
INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
SELECT s.id, e.id, v.source, s.ouverture_at + INTERVAL '5 minutes'
FROM (VALUES ('DEMO1A', 'Alice Mbarga', 'ETUDIANT'),
             ('DEMO1A', 'Bruno Tchoupo', 'ETUDIANT'),
             ('DEMO1A', 'Carine Ngo', 'ETUDIANT'),
             ('DEMO1A', 'David Essomba', 'ETUDIANT'),
             ('DEMO1A', 'Estelle Fotso', 'ETUDIANT'),
             ('DEMO1A', 'Franck Nana', 'FORMATEUR'),
             ('DEMO2B', 'Alice Mbarga', 'ETUDIANT'),
             ('DEMO2B', 'Bruno Tchoupo', 'ETUDIANT'),
             ('DEMO2B', 'Carine Ngo', 'ETUDIANT'),
             ('DEMO2B', 'Estelle Fotso', 'ETUDIANT')) AS v(code, nom, source)
JOIN session_cours s ON s.code = v.code
JOIN etudiant e ON e.nom = v.nom AND e.promotion_id = s.promotion_id
WHERE NOT EXISTS (SELECT 1 FROM presence p WHERE p.session_id = s.id AND p.etudiant_id = e.id);

-- Exercices
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at)
SELECT s.id, e.id, v.lien, v.statut, s.ouverture_at + INTERVAL '2 hours'
FROM (VALUES ('DEMO1A', 'Alice Mbarga', 'https://github.com/alice-mbarga/tp-git', 'RELU'),
             ('DEMO1A', 'Bruno Tchoupo', 'https://github.com/bruno-tchoupo/tp-git', 'RELU'),
             ('DEMO1A', 'Carine Ngo', 'https://github.com/carine-ngo/tp-git', 'RELU'),
             ('DEMO1A', 'David Essomba', 'https://github.com/david-essomba/tp-git', 'EN_ATTENTE_RELECTURE'),
             ('DEMO2B', 'Alice Mbarga', 'https://github.com/alice-mbarga/api-rest', 'EN_ATTENTE_RELECTURE'),
             ('DEMO2B', 'Bruno Tchoupo', 'https://github.com/bruno-tchoupo/api-rest', 'RELU')) AS v(code, nom, lien, statut)
JOIN session_cours s ON s.code = v.code
JOIN etudiant e ON e.nom = v.nom AND e.promotion_id = s.promotion_id
WHERE NOT EXISTS (SELECT 1 FROM exercice x WHERE x.session_id = s.id AND x.etudiant_id = e.id);

-- Relectures (note NULL = pas encore rendue, visible « en attente » : Q11)
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, assignee_at, rendue_at)
SELECT x.id, r.id, v.note, v.commentaire, x.depose_at,
       CASE WHEN v.note IS NULL THEN NULL ELSE x.depose_at + INTERVAL '1 hour' END
FROM (VALUES ('DEMO1A', 'Alice Mbarga', 'Bruno Tchoupo', 15, 'Historique propre, messages de commit clairs.'),
             ('DEMO1A', 'Bruno Tchoupo', 'Carine Ngo', 12, 'Branches correctes, mais le README est incomplet.'),
             ('DEMO1A', 'Carine Ngo', 'David Essomba', 17, 'Très bon travail, PR bien décrites.'),
             ('DEMO1A', 'David Essomba', 'Estelle Fotso', NULL, NULL),
             ('DEMO2B', 'Alice Mbarga', 'Carine Ngo', NULL, NULL),
             ('DEMO2B', 'Bruno Tchoupo', 'Estelle Fotso', 14, 'Endpoints conformes, gestion d''erreurs à compléter.'))
       AS v(code, auteur, relecteur, note, commentaire)
JOIN session_cours s ON s.code = v.code
JOIN etudiant a ON a.nom = v.auteur AND a.promotion_id = s.promotion_id
JOIN exercice x ON x.session_id = s.id AND x.etudiant_id = a.id
JOIN etudiant r ON r.nom = v.relecteur AND r.promotion_id = s.promotion_id
WHERE NOT EXISTS (SELECT 1 FROM relecture l WHERE l.exercice_id = x.id);
