package com.kfokam48.presencelab.dto;

import com.kfokam48.presencelab.entite.StatutExercice;

/** Réponse imposée de POST /api/exercices : { id, statut }. */
public record ExerciceDeposeDto(Long id, StatutExercice statut) {
}
