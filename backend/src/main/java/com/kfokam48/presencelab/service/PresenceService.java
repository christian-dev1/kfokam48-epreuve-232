package com.kfokam48.presencelab.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.dto.PresenceDto;
import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Presence;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.entite.SourcePresence;
import com.kfokam48.presencelab.erreur.CodeExpireException;
import com.kfokam48.presencelab.erreur.CodeInconnuException;
import com.kfokam48.presencelab.erreur.DejaPresentException;
import com.kfokam48.presencelab.erreur.MetierException;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.PresenceRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

/**
 * EF3 — Marquage de présence par code. Ordre des contrôles (H6, diagramme D3) :
 * code inconnu (400) → code expiré ou session clôturée (410) → déjà présent (409) → 201.
 */
@Service
@Transactional
public class PresenceService {

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final Clock horloge;

    public PresenceService(SessionCoursRepository sessions, EtudiantRepository etudiants,
                           PresenceRepository presences, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.horloge = horloge;
    }

    public PresenceDto marquerPresence(String code, Long etudiantId) {
        Instant maintenant = Instant.now(horloge);
        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(() -> MetierException.requeteInvalide("ETUDIANT_INCONNU", "Cet étudiant n'existe pas."));

        SessionCours session = sessions.findByCode(normaliser(code))
                .orElseThrow(CodeInconnuException::new);

        // RG1 + RG2 : plus aucune présence par code après expiration ou clôture
        if (session.codeExpire(maintenant) || session.estCloturee()) {
            throw new CodeExpireException();
        }
        // H7 : le code d'une autre promotion ne vaut pas pour cet étudiant
        if (!Objects.equals(etudiant.getPromotion().getId(), session.getPromotion().getId())) {
            throw MetierException.requeteInvalide("ETUDIANT_HORS_PROMOTION",
                    "Cette session ne concerne pas ta promotion.");
        }
        // RG3 : une seule présence par session
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new DejaPresentException();
        }
        Presence presence = presences.save(new Presence(session, etudiant, SourcePresence.ETUDIANT, maintenant));
        return PresenceDto.depuis(presence);
    }

    /** Saisie sur téléphone : espaces et minuscules tolérés. */
    static String normaliser(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
