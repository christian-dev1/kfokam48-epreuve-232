package com.kfokam48.presencelab.dto;

import java.time.Instant;

import com.kfokam48.presencelab.entite.SessionCours;

public record SessionDto(Long id, String titre, Long promotionId, String code,
                         Instant ouvertureAt, Instant expirationAt, Instant clotureAt, boolean cloturee) {

    public static SessionDto depuis(SessionCours s) {
        return new SessionDto(s.getId(), s.getTitre(), s.getPromotion().getId(), s.getCode(),
                s.getOuvertureAt(), s.getExpirationAt(), s.getClotureAt(), s.estCloturee());
    }
}
