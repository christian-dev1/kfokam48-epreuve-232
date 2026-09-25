package com.kfokam48.presencelab.controleur;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kfokam48.presencelab.dto.LigneTableauDto;
import com.kfokam48.presencelab.service.TableauService;

@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService service;

    public TableauController(TableauService service) {
        this.service = service;
    }

    /** Opération imposée : 200 [ lignes ] · 404 PROMOTION_INCONNUE. */
    @GetMapping
    public List<LigneTableauDto> tableau(@RequestParam Long promotionId) {
        return service.tableau(promotionId);
    }
}
