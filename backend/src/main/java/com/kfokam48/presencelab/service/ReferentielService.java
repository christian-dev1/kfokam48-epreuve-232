package com.kfokam48.presencelab.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.dto.EtudiantDto;
import com.kfokam48.presencelab.dto.PromotionDto;
import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Promotion;
import com.kfokam48.presencelab.erreur.MetierException;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.PromotionRepository;

/** Promotions et étudiants : la liste dans laquelle l'étudiant choisit son nom (EF2, Q1, H1). */
@Service
@Transactional(readOnly = true)
public class ReferentielService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public ReferentielService(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    public List<PromotionDto> listerPromotions() {
        return promotions.findAllByOrderByNomAsc().stream()
                .map(p -> new PromotionDto(p.getId(), p.getNom()))
                .toList();
    }

    public List<EtudiantDto> listerEtudiants(Long promotionId) {
        promotionExistante(promotionId);
        return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(e -> new EtudiantDto(e.getId(), e.getNom()))
                .toList();
    }

    /** 404 PROMOTION_INCONNUE si la promotion n'existe pas. */
    public Promotion promotionExistante(Long promotionId) {
        return promotions.findById(promotionId)
                .orElseThrow(() -> MetierException.introuvable("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));
    }

    /** 404 ETUDIANT_INCONNU si l'étudiant n'existe pas. */
    public Etudiant etudiantExistant(Long etudiantId) {
        return etudiants.findById(etudiantId)
                .orElseThrow(() -> MetierException.introuvable("ETUDIANT_INCONNU", "Cet étudiant n'existe pas."));
    }
}
