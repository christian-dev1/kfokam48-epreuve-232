package com.kfokam48.presencelab.erreur;

import org.springframework.http.HttpStatus;

/** RG3 — voir D3. */
public class DejaPresentException extends MetierException {

    public DejaPresentException() {
        super(HttpStatus.CONFLICT, "DEJA_PRESENT", "Ta présence est déjà enregistrée pour cette session.");
    }
}
