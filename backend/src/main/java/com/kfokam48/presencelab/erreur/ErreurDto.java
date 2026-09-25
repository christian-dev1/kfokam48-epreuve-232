package com.kfokam48.presencelab.erreur;

/** Format d'erreur imposé par le contrat, pour TOUTES les erreurs : { code, message }. */
public record ErreurDto(String code, String message) {
}
