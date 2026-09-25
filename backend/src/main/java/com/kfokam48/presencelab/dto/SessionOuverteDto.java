package com.kfokam48.presencelab.dto;

import java.time.Instant;

/** Réponse imposée de POST /api/sessions : { id, code, ouvertureAt, expirationAt }. */
public record SessionOuverteDto(Long id, String code, Instant ouvertureAt, Instant expirationAt) {
}
