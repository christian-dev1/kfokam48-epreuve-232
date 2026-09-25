package com.kfokam48.presencelab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MarquerPresenceRequete(@NotBlank String code, @NotNull Long etudiantId) {
}
