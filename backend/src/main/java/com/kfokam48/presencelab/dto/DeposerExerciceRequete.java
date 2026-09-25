package com.kfokam48.presencelab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeposerExerciceRequete(@NotNull Long sessionId, @NotNull Long etudiantId, @NotBlank String lien) {
}
