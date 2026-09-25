package com.kfokam48.presencelab.erreur;

import org.springframework.http.HttpStatus;

/** RG5 (compte comme une erreur de saisie) — voir D3. */
public class CodeInconnuException extends MetierException {

    public CodeInconnuException() {
        super(HttpStatus.BAD_REQUEST, "CODE_INCONNU", "Ce code de présence n'existe pas.");
    }
}
