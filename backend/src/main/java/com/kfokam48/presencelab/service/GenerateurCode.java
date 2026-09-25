package com.kfokam48.presencelab.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * ENF7 : code de 6 caractères parmi 32 (A-Z sans I ni O, chiffres 2-9), soit environ 10^9 combinaisons,
 * tiré par SecureRandom. Les caractères ambigus (0/O, 1/I) sont exclus pour la saisie sur téléphone.
 */
@Component
public class GenerateurCode {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int LONGUEUR = 6;

    private final SecureRandom aleatoire = new SecureRandom();

    public String nouveauCode() {
        StringBuilder code = new StringBuilder(LONGUEUR);
        for (int i = 0; i < LONGUEUR; i++) {
            code.append(ALPHABET.charAt(aleatoire.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
