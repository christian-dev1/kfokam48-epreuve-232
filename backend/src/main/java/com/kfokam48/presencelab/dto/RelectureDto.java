package com.kfokam48.presencelab.dto;

import com.kfokam48.presencelab.entite.Relecture;

/** Vue du relecteur : jamais le nom de l'auteur (anonymat réciproque, RG15). */
public record RelectureDto(Long id, Long exerciceId, String sessionTitre, String lien,
                           boolean rendue, Integer note, String commentaire) {

    public static RelectureDto depuis(Relecture r) {
        return new RelectureDto(r.getId(), r.getExercice().getId(), r.getExercice().getSession().getTitre(),
                r.getExercice().getLien(), r.estRendue(), r.getNote(), r.getCommentaire());
    }
}
