package com.kfokam48.presencelab.erreur;

import org.springframework.http.HttpStatus;

/** RG1, RG2 — voir D3. */
public class CodeExpireException extends MetierException {

    public CodeExpireException() {
        super(HttpStatus.GONE, "CODE_EXPIRE", "Le code de présence a expiré.");
    }
}
