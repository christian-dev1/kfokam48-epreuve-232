package com.kfokam48.presencelab.controleur;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kfokam48.presencelab.dto.OuvrirSessionRequete;
import com.kfokam48.presencelab.dto.SessionDto;
import com.kfokam48.presencelab.dto.SessionOuverteDto;
import com.kfokam48.presencelab.service.SessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    /** Opération imposée : 201 { id, code, ouvertureAt, expirationAt } · 400 champ manquant. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionOuverteDto ouvrir(@Valid @RequestBody OuvrirSessionRequete requete) {
        return service.ouvrir(requete);
    }

    @GetMapping
    public List<SessionDto> lister(@RequestParam Long promotionId) {
        return service.lister(promotionId);
    }
}
