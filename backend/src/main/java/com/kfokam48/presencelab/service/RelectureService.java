package com.kfokam48.presencelab.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.dto.RelectureDto;
import com.kfokam48.presencelab.dto.RendreRelectureRequete;
import com.kfokam48.presencelab.entite.Relecture;
import com.kfokam48.presencelab.erreur.MetierException;
import com.kfokam48.presencelab.repository.RelectureRepository;

/** EF6, EF7 — Relectures assignées et rendu de la note. */
@Service
@Transactional
public class RelectureService {

    private final RelectureRepository relectures;
    private final ReferentielService referentiel;
    private final Clock horloge;

    public RelectureService(RelectureRepository relectures, ReferentielService referentiel, Clock horloge) {
        this.relectures = relectures;
        this.referentiel = referentiel;
        this.horloge = horloge;
    }

    /** EF6 : à faire d'abord, puis les plus anciennes. */
    @Transactional(readOnly = true)
    public List<RelectureDto> lister(Long relecteurId) {
        referentiel.etudiantExistant(relecteurId);
        return relectures.assigneesA(relecteurId).stream()
                .sorted(Comparator.comparing(Relecture::estRendue).thenComparing(Relecture::getAssigneeAt))
                .map(RelectureDto::depuis)
                .toList();
    }

    /** EF7 : 403 AUTO_RELECTURE (RG12) · 409 RELECTURE_DEJA_RENDUE (RG14, C1) · 404 RELECTURE_INCONNUE. */
    public RelectureDto rendre(Long id, RendreRelectureRequete requete) {
        Relecture relecture = relectures.findById(id)
                .orElseThrow(() -> MetierException.introuvable("RELECTURE_INCONNUE", "Cette relecture n'existe pas."));
        Long auteurId = relecture.getExercice().getEtudiant().getId();
        Long relecteurAssigneId = relecture.getRelecteur().getId();

        // RG12 : double protection — l'appelant déclaré (H5) et le relecteur assigné ne sont jamais l'auteur
        if (Objects.equals(requete.relecteurId(), auteurId) || Objects.equals(relecteurAssigneId, auteurId)) {
            throw MetierException.interdit("AUTO_RELECTURE", "Tu ne peux pas relire ton propre exercice.");
        }
        if (requete.relecteurId() != null && !Objects.equals(requete.relecteurId(), relecteurAssigneId)) {
            throw MetierException.interdit("RELECTEUR_NON_ASSIGNE", "Cette relecture est assignée à un autre étudiant.");
        }
        if (relecture.estRendue()) { // RG14 : définitive (contradiction C1 tranchée pour Q15)
            throw MetierException.conflit("RELECTURE_DEJA_RENDUE", "Cette relecture a déjà été rendue : elle est définitive.");
        }
        relecture.rendre(requete.note(), requete.commentaire().trim(), Instant.now(horloge));
        // D4 v2 : première note → PARTIELLEMENT_RELU (provisoire, RG20), seconde → RELU (RG19)
        long notesRendues = relectures.countByExerciceIdAndRendueAtIsNotNull(relecture.getExercice().getId());
        relecture.getExercice().enregistrerNotesRendues(notesRendues);
        return RelectureDto.depuis(relecture);
    }
}
