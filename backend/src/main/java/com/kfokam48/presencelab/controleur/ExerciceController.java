package com.kfokam48.presencelab.controleur;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kfokam48.presencelab.dto.DeposerExerciceRequete;
import com.kfokam48.presencelab.dto.ExerciceDeposeDto;
import com.kfokam48.presencelab.service.ExerciceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    /** Opération imposée : 201 { id, statut } · 400 LIEN_INVALIDE · 409 EXERCICE_DEJA_DEPOSE. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciceDeposeDto deposer(@Valid @RequestBody DeposerExerciceRequete requete) {
        return service.deposer(requete);
    }
}
