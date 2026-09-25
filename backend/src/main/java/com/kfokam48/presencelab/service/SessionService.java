package com.kfokam48.presencelab.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.dto.OuvrirSessionRequete;
import com.kfokam48.presencelab.dto.SessionDto;
import com.kfokam48.presencelab.dto.SessionOuverteDto;
import com.kfokam48.presencelab.entite.Promotion;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.erreur.MetierException;
import com.kfokam48.presencelab.repository.PromotionRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

@Service
@Transactional
public class SessionService {

    private static final int TENTATIVES_CODE_UNIQUE = 10;

    private final SessionCoursRepository sessions;
    private final PromotionRepository promotions;
    private final ReferentielService referentiel;
    private final GenerateurCode generateur;
    private final Clock horloge;

    public SessionService(SessionCoursRepository sessions, PromotionRepository promotions,
                          ReferentielService referentiel, GenerateurCode generateur, Clock horloge) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.referentiel = referentiel;
        this.generateur = generateur;
        this.horloge = horloge;
    }

    /** EF1 : ouvre une session et renvoie son code ; expiration = ouverture + 15 min (RG1). */
    public SessionOuverteDto ouvrir(OuvrirSessionRequete requete) {
        Promotion promotion = promotions.findById(requete.promotionId())
                .orElseThrow(() -> MetierException.requeteInvalide("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));
        Instant maintenant = Instant.now(horloge).truncatedTo(ChronoUnit.SECONDS);
        SessionCours session = sessions.save(new SessionCours(requete.titre().trim(), promotion, codeUnique(), maintenant));
        return new SessionOuverteDto(session.getId(), session.getCode(), session.getOuvertureAt(), session.getExpirationAt());
    }

    @Transactional(readOnly = true)
    public List<SessionDto> lister(Long promotionId) {
        referentiel.promotionExistante(promotionId);
        return sessions.findByPromotionIdOrderByOuvertureAtDesc(promotionId).stream()
                .map(SessionDto::depuis)
                .toList();
    }

    /** RG18 : le code est unique ; la contrainte uk_session_code reste le dernier rempart. */
    private String codeUnique() {
        for (int i = 0; i < TENTATIVES_CODE_UNIQUE; i++) {
            String code = generateur.nouveauCode();
            if (!sessions.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code de session unique");
    }
}
