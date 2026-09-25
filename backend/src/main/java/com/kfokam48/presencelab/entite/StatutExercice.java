package com.kfokam48.presencelab.entite;

/** Cycle de vie d'un exercice — diagramme D4. */
public enum StatutExercice {
    DEPOSE,
    EN_ATTENTE_RELECTURE,
    /** v2 (étape 3) : une note rendue sur deux, affichée comme provisoire (RG20). */
    PARTIELLEMENT_RELU,
    RELU
}
