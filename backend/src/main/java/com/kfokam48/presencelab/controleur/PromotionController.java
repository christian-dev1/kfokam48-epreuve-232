package com.kfokam48.presencelab.controleur;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kfokam48.presencelab.dto.EtudiantDto;
import com.kfokam48.presencelab.dto.PromotionDto;
import com.kfokam48.presencelab.service.ReferentielService;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final ReferentielService referentiel;

    public PromotionController(ReferentielService referentiel) {
        this.referentiel = referentiel;
    }

    @GetMapping
    public List<PromotionDto> lister() {
        return referentiel.listerPromotions();
    }

    @GetMapping("/{id}/etudiants")
    public List<EtudiantDto> etudiants(@PathVariable Long id) {
        return referentiel.listerEtudiants(id);
    }
}
