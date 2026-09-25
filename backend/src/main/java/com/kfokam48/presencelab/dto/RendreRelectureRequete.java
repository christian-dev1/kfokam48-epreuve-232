package com.kfokam48.presencelab.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps imposé { note, commentaire } + relecteurId facultatif (H5).
 * RG13 : note entière de 0 à 20 ; une note décimale est rejetée par Jackson (accept-float-as-int=false).
 */
public record RendreRelectureRequete(
        @NotNull @Min(0) @Max(20) Integer note,
        @NotBlank @Size(max = 1000) String commentaire,
        Long relecteurId) {
}
