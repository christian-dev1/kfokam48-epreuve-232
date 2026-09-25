package com.kfokam48.presencelab.dto;

import com.kfokam48.presencelab.entite.Presence;
import com.kfokam48.presencelab.entite.SourcePresence;

/** Réponse imposée : { id, sessionId, etudiantId, source }. */
public record PresenceDto(Long id, Long sessionId, Long etudiantId, SourcePresence source) {

    public static PresenceDto depuis(Presence p) {
        return new PresenceDto(p.getId(), p.getSession().getId(), p.getEtudiant().getId(), p.getSource());
    }
}
