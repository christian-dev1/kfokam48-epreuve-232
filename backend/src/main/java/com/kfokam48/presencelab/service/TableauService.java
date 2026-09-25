package com.kfokam48.presencelab.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.dto.LigneTableauDto;
import com.kfokam48.presencelab.entite.StatutExercice;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.ExerciceRepository;
import com.kfokam48.presencelab.repository.PresenceRepository;
import com.kfokam48.presencelab.repository.RelectureRepository;

/**
 * EF8 — Tableau du formateur. Cinq requêtes agrégées quelle que soit la taille de la promotion
 * (pas de N+1, ENF2). La moyenne est calculée ici et nulle part ailleurs (F3).
 */
@Service
@Transactional(readOnly = true)
public class TableauService {

    private final ReferentielService referentiel;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public TableauService(ReferentielService referentiel, EtudiantRepository etudiants, PresenceRepository presences,
                          ExerciceRepository exercices, RelectureRepository relectures) {
        this.referentiel = referentiel;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    public List<LigneTableauDto> tableau(Long promotionId) {
        referentiel.promotionExistante(promotionId); // 404 PROMOTION_INCONNUE

        Map<Long, Long> nbPresences = versMap(presences.compterParEtudiant(promotionId));
        Map<Long, Long> nbExercices = versMap(exercices.compterParEtudiant(promotionId));
        Map<Long, Long> nbNonRelus = versMap(exercices.compterNonRelusParEtudiant(promotionId, StatutExercice.RELU));
        Map<Long, Long> nbAFaire = versMap(relectures.enAttenteParRelecteur(promotionId));
        Map<Long, Double> moyennes = new HashMap<>();
        for (Object[] ligne : relectures.moyenneParAuteur(promotionId)) {
            moyennes.put(((Number) ligne[0]).longValue(), arrondir(((Number) ligne[1]).doubleValue()));
        }

        return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(e -> new LigneTableauDto(
                        e.getId(),
                        e.getNom(),
                        nbPresences.getOrDefault(e.getId(), 0L).intValue(),
                        nbExercices.getOrDefault(e.getId(), 0L).intValue(),
                        moyennes.get(e.getId()),
                        nbAFaire.getOrDefault(e.getId(), 0L).intValue(),
                        nbNonRelus.getOrDefault(e.getId(), 0L).intValue()))
                .toList();
    }

    /** RG16 : arrondi à 2 décimales. */
    static double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    private static Map<Long, Long> versMap(List<Object[]> lignes) {
        Map<Long, Long> resultat = new HashMap<>();
        for (Object[] ligne : lignes) {
            resultat.put(((Number) ligne[0]).longValue(), ((Number) ligne[1]).longValue());
        }
        return resultat;
    }
}
