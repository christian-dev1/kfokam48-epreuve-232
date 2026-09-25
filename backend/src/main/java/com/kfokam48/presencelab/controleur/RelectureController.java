package com.kfokam48.presencelab.controleur;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kfokam48.presencelab.dto.RelectureDto;
import com.kfokam48.presencelab.dto.RendreRelectureRequete;
import com.kfokam48.presencelab.service.RelectureService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    @GetMapping
    public List<RelectureDto> lister(@RequestParam Long relecteurId) {
        return service.lister(relecteurId);
    }

    /** Opération imposée : 200 · 400 NOTE_INVALIDE · 403 AUTO_RELECTURE · 409 RELECTURE_DEJA_RENDUE. */
    @PostMapping("/{id}")
    public RelectureDto rendre(@PathVariable Long id, @Valid @RequestBody RendreRelectureRequete requete) {
        return service.rendre(id, requete);
    }
}
