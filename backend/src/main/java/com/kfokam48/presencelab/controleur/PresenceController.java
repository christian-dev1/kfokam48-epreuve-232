package com.kfokam48.presencelab.controleur;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kfokam48.presencelab.dto.MarquerPresenceRequete;
import com.kfokam48.presencelab.dto.PresenceDto;
import com.kfokam48.presencelab.service.PresenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    /** Opération imposée : 201 · 400 CODE_INCONNU · 409 DEJA_PRESENT · 410 CODE_EXPIRE. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceDto marquer(@Valid @RequestBody MarquerPresenceRequete requete) {
        return service.marquerPresence(requete.code(), requete.etudiantId());
    }
}
