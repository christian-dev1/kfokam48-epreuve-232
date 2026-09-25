package com.kfokam48.presencelab.dto;

/**
 * Ligne imposée du tableau : { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente }
 * + exercicesEnAttente (ajout H9). moyenne vaut null si aucune note reçue (RG16).
 */
public record LigneTableauDto(Long etudiantId, String nom, int presences, int exercicesDeposes,
                              Double moyenne, int relecturesEnAttente, int exercicesEnAttente) {
}
