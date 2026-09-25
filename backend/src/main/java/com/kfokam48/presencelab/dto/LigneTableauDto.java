package com.kfokam48.presencelab.dto;

/**
 * Ligne imposée du tableau : { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente }
 * + exercicesEnAttente (ajout H9) + moyenneProvisoire (v2, RG20).
 * moyenne vaut null si aucune note reçue (RG16) ; elle porte sur les notes retenues (RG19).
 */
public record LigneTableauDto(Long etudiantId, String nom, int presences, int exercicesDeposes,
                              Double moyenne, int relecturesEnAttente, int exercicesEnAttente,
                              boolean moyenneProvisoire) {
}
