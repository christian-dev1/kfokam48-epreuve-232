package com.kfokam48.presencelab.erreur;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : porte le statut HTTP et le code d'erreur stable du contrat.
 * Traduite en { code, message } par {@link GestionErreurs}.
 */
public class MetierException extends RuntimeException {

    private final HttpStatus statut;
    private final String code;

    public MetierException(HttpStatus statut, String code, String message) {
        super(message);
        this.statut = statut;
        this.code = code;
    }

    public HttpStatus getStatut() {
        return statut;
    }

    public String getCode() {
        return code;
    }

    public static MetierException introuvable(String code, String message) {
        return new MetierException(HttpStatus.NOT_FOUND, code, message);
    }

    public static MetierException requeteInvalide(String code, String message) {
        return new MetierException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static MetierException conflit(String code, String message) {
        return new MetierException(HttpStatus.CONFLICT, code, message);
    }

    public static MetierException interdit(String code, String message) {
        return new MetierException(HttpStatus.FORBIDDEN, code, message);
    }
}
