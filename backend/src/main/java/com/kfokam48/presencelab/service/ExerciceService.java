package com.kfokam48.presencelab.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.dto.DeposerExerciceRequete;
import com.kfokam48.presencelab.dto.ExerciceDeposeDto;
import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Exercice;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.erreur.MetierException;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.ExerciceRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

/** EF4 — Dépôt du lien d'un exercice, suivi du tirage du relecteur (EF5). */
@Service
@Transactional
public class ExerciceService {

    static final int LONGUEUR_MAX_LIEN = 500;

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final ExerciceRepository exercices;
    private final AssignationRelecteur assignation;
    private final Clock horloge;

    public ExerciceService(SessionCoursRepository sessions, EtudiantRepository etudiants,
                           ExerciceRepository exercices, AssignationRelecteur assignation, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.exercices = exercices;
        this.assignation = assignation;
        this.horloge = horloge;
    }

    public ExerciceDeposeDto deposer(DeposerExerciceRequete requete) {
        SessionCours session = sessions.findById(requete.sessionId())
                .orElseThrow(() -> MetierException.requeteInvalide("SESSION_INCONNUE", "Cette session n'existe pas."));
        Etudiant auteur = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> MetierException.requeteInvalide("ETUDIANT_INCONNU", "Cet étudiant n'existe pas."));
        if (!Objects.equals(auteur.getPromotion().getId(), session.getPromotion().getId())) {
            throw MetierException.requeteInvalide("ETUDIANT_HORS_PROMOTION", "Cette session ne concerne pas ta promotion.");
        }
        String lien = requete.lien().trim();
        if (!lienValide(lien)) { // RG7
            throw MetierException.requeteInvalide("LIEN_INVALIDE", "Le lien doit être une adresse http ou https valide.");
        }
        if (session.estCloturee()) { // RG8 : dépôt possible jusqu'à la clôture, pas au-delà (Q12)
            throw MetierException.conflit("SESSION_CLOTUREE", "Cette session est clôturée : le dépôt n'est plus possible.");
        }
        if (exercices.existsBySessionIdAndEtudiantId(session.getId(), auteur.getId())) { // RG6
            throw MetierException.conflit("EXERCICE_DEJA_DEPOSE", "Tu as déjà déposé un exercice pour cette session.");
        }
        Exercice exercice = exercices.save(new Exercice(session, auteur, lien, Instant.now(horloge)));
        assignation.assigner(exercice); // EF5, dans la même transaction que le dépôt
        return new ExerciceDeposeDto(exercice.getId(), exercice.getStatut());
    }

    /** RG7 : URL absolue en http ou https, avec un hôte, de 500 caractères au plus. */
    static boolean lienValide(String lien) {
        if (lien == null || lien.isBlank() || lien.length() > LONGUEUR_MAX_LIEN) {
            return false;
        }
        try {
            URI uri = new URI(lien);
            String schema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            return (schema.equals("http") || schema.equals("https"))
                    && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
